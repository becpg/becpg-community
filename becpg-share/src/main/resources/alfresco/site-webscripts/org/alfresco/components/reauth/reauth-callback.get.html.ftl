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
      if ( window.opener) {
        window.opener.postMessage({ "SUCCESS" }, window.origin);
        window.close();
      } else {
        // document.body will be available now
        document.body.innerHTML = "<p>Authentication  invalid.</p>";
      }
    })();
  </script>
</body>
</html>