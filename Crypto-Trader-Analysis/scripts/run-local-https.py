import os
import ssl
import sys
from pathlib import Path
from wsgiref.simple_server import make_server


analysis_root = Path(__file__).resolve().parents[1]
repo_root = analysis_root.parent
src_dir = analysis_root / "src"

sys.path.insert(0, str(analysis_root))
sys.path.insert(0, str(src_dir))
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "crypto_trader_analysis.api.settings")

from django.core.wsgi import get_wsgi_application  # noqa: E402


def env_path(name: str, default: Path) -> Path:
    return Path(os.getenv(name, str(default))).expanduser().resolve()


host = os.getenv("CT_ANALYSIS_HOST", "0.0.0.0")
port = int(os.getenv("CT_ANALYSIS_PORT", "8000"))
cert_file = env_path("CT_ANALYSIS_CERT_FILE", repo_root / "certs" / "localhost.pem")
key_file = env_path("CT_ANALYSIS_KEY_FILE", repo_root / "certs" / "localhost-key.pem")

if not cert_file.exists():
    raise FileNotFoundError(f"TLS certificate not found: {cert_file}")
if not key_file.exists():
    raise FileNotFoundError(f"TLS private key not found: {key_file}")

application = get_wsgi_application()
context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
context.load_cert_chain(certfile=cert_file, keyfile=key_file)

with make_server(host, port, application) as server:
    server.socket = context.wrap_socket(server.socket, server_side=True)
    print(f"Crypto-Trader-Analysis HTTPS server listening on https://localhost:{port}")
    print(f"Certificate: {cert_file}")
    server.serve_forever()
