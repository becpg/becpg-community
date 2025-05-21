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
      if (window.opener) {
        try {
          // Send success message with code and source identifier
          window.opener.postMessage({ 
            source: 'beCPG-reauth',
            code: 'REAUTH_SUCCESS' 
          }, window.origin);
          // Small delay to ensure message is sent before closing
          setTimeout(function() {
            window.close();
          }, 100);
        } catch (e) {
          console.error("Error sending message to parent:", e);
          window.close();
        }
      } else {
        document.body.innerHTML = "<p>Authentication invalid.</p>";
      }
    })();
  </script>
</body>
</html>