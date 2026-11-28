# API

The full OpenAPI specification for Crypto Trader is embedded below.

<div class="fullwidth">
  <div id="redoc"></div>
</div>

<script src="https://cdn.jsdelivr.net/npm/redoc@2.5.0/bundles/redoc.standalone.js"></script>
<script>
  document.addEventListener('DOMContentLoaded', function() {
    Redoc.init('openapi-api.yaml', {
      hideHostname: true,
      expandResponses: '200,201',
      pathInMiddlePanel: true,
      theme: {
        spacing: { sectionVertical: 12 },
        colors: { primary: { main: '#3f51b5' } },
        typography: { fontSize: '14px' }
      }
    }, document.getElementById('redoc'));
  });
</script>
