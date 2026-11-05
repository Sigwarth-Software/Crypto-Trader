from decimal import Decimal
from io import BytesIO
import json
import unittest
from unittest.mock import patch
from zipfile import ZipFile

from scripts.ai_detection.codequiry import CodequiryClient, CodequiryError


class CodequiryClientTests(unittest.TestCase):
    def setUp(self):
        https = self.enterContext(patch("scripts.ai_detection.codequiry.HTTPSConnection"))
        self.connection = https.return_value
        self.response = self.connection.getresponse.return_value
        self.response.status = 200
        self.response.read.return_value = b'{"success":true}'
        # Dummy credential for mocked requests. This value cannot access an account.
        self.client = CodequiryClient("fake-codequiry-key")

    def payload(self):
        return json.loads(self.connection.request.call_args.kwargs["body"])

    def test_discovery_uses_fixed_host_and_api_key_header(self):
        self.client.account()
        args, kwargs = self.connection.request.call_args
        self.assertEqual(args, ("GET", "/api/v1/account"))
        self.assertEqual(kwargs["headers"]["apikey"], "fake-codequiry-key")
        self.client.languages()
        self.assertEqual(self.connection.request.call_args.args[1], "/api/v1/languages")
        self.client.test_types()
        self.assertEqual(self.connection.request.call_args.args[1], "/api/v1/test-types")

    def test_create_enables_ai_and_returns_job_id(self):
        self.response.status = 201
        self.response.read.return_value = b'{"id":42}'
        self.assertEqual(self.client.create_check("Synthetic check", language=13, test_type=9), 42)
        self.assertEqual(self.payload(), {
            "name": "Synthetic check", "language": 13, "test_type": 9, "ai_run": True,
        })

    def test_upload_encodes_zip_and_id_as_multipart(self):
        buffer = BytesIO()
        with ZipFile(buffer, "w") as archive:
            archive.writestr("submission/component_1.py", "print(1)")
        self.client.upload(42, buffer.getvalue())
        kwargs = self.connection.request.call_args.kwargs
        self.assertIn(b'name="check_id"\r\n\r\n42', kwargs["body"])
        self.assertIn(buffer.getvalue(), kwargs["body"])
        self.assertIn("boundary=", kwargs["headers"]["Content-Type"])
        with self.assertRaises(ValueError):
            self.client.upload(42, b"invalid zip")

    def test_start_and_read_operations_keep_correct_id_fields(self):
        self.client.start(42, test_type=9)
        self.assertEqual(self.payload(), {"check_id": 42, "test_type": 9})
        self.client.get_check(42)
        self.assertEqual(self.payload(), {"check_id": 42})
        self.response.read.return_value = b'{"score":85.20}'
        self.assertEqual(self.client.ai_results(42)["score"], Decimal("85.20"))
        self.assertEqual(self.payload(), {"assignment_id": 42})

    def test_errors_are_sanitized_and_never_automatically_retried(self):
        for status in (302, 401, 429, 503):
            with self.subTest(status=status):
                self.connection.reset_mock()
                self.response.status = status
                self.response.read.return_value = b"private source and fake-codequiry-key"
                with self.assertRaisesRegex(CodequiryError, f"HTTP {status}") as caught:
                    self.client.start(42, test_type=9)
                self.assertNotIn("fake-codequiry-key", str(caught.exception))
                self.assertEqual(self.connection.request.call_count, 1)
                self.connection.close.assert_called_once()

    def test_bad_responses_fail_explicitly(self):
        for data in (b"not json", b"null", b'{"success":false}'):
            with self.subTest(data=data):
                self.response.read.return_value = data
                with self.assertRaises(CodequiryError):
                    self.client.account()

    def test_timeout_does_not_repeat_mutation(self):
        self.connection.getresponse.side_effect = TimeoutError()
        with self.assertRaises(CodequiryError):
            self.client.create_check("Synthetic", language=13, test_type=9)
        self.assertEqual(self.connection.request.call_count, 1)

    def test_config_validation_happens_before_request(self):
        with self.assertRaises(ValueError):
            CodequiryClient("")
        with self.assertRaises(ValueError):
            self.client.start(-1, test_type=9)
        self.connection.request.assert_not_called()
