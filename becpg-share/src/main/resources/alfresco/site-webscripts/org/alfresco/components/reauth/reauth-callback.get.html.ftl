<!DOCTYPE html>
<html>
<head>
  <title>Re-authentication...</title>
  <!-- Script removed from here -->
</head>
<body>
  <noscript>This page requires JavaScript to work.</noscript>

  <script type="text/javascript">
    (function () {
      const urlParams = new URLSearchParams(window.location.search);
      const code = urlParams.get('code');
      if (code && window.opener) {
        window.opener.postMessage({ code }, window.origin);
        window.close();
      } else {
        // document.body will be available now
        document.body.innerHTML = "<p>Authentication code missing or invalid.</p>";
      }
    })();
  </script>
</body>
</html>