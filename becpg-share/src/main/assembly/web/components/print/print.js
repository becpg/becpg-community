/******/ (function(modules) { // webpackBootstrap
/******/ 	// The module cache
/******/ 	var installedModules = {};
/******/
/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {
/******/
/******/ 		// Check if module is in cache
/******/ 		if(installedModules[moduleId]) {
/******/ 			return installedModules[moduleId].exports;
/******/ 		}
/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = installedModules[moduleId] = {
/******/ 			i: moduleId,
/******/ 			l: false,
/******/ 			exports: {}
/******/ 		};
/******/
/******/ 		// Execute the module function
/******/ 		modules[moduleId].call(module.exports, module, module.exports, __webpack_require__);
/******/
/******/ 		// Flag the module as loaded
/******/ 		module.l = true;
/******/
/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}
/******/
/******/
/******/ 	// expose the modules object (__webpack_modules__)
/******/ 	__webpack_require__.m = modules;
/******/
/******/ 	// expose the module cache
/******/ 	__webpack_require__.c = installedModules;
/******/
/******/ 	// define getter function for harmony exports
/******/ 	__webpack_require__.d = function(exports, name, getter) {
/******/ 		if(!__webpack_require__.o(exports, name)) {
/******/ 			Object.defineProperty(exports, name, {
/******/ 				configurable: false,
/******/ 				enumerable: true,
/******/ 				get: getter
/******/ 			});
/******/ 		}
/******/ 	};
/******/
/******/ 	// getDefaultExport function for compatibility with non-harmony modules
/******/ 	__webpack_require__.n = function(module) {
/******/ 		var getter = module && module.__esModule ?
/******/ 			function getDefault() { return module['default']; } :
/******/ 			function getModuleExports() { return module; };
/******/ 		__webpack_require__.d(getter, 'a', getter);
/******/ 		return getter;
/******/ 	};
/******/
/******/ 	// Object.prototype.hasOwnProperty.call
/******/ 	__webpack_require__.o = function(object, property) { return Object.prototype.hasOwnProperty.call(object, property); };
/******/
/******/ 	// __webpack_public_path__
/******/ 	__webpack_require__.p = "./";
/******/
/******/ 	// Load entry module and return exports
/******/ 	return __webpack_require__(__webpack_require__.s = 4);
/******/ })
/************************************************************************/
/******/ ([
/* 0 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__browser__ = __webpack_require__(1);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__modal__ = __webpack_require__(3);



var Print = {
  send: function send(params, printFrame) {
    // Append iframe element to document body
    document.getElementsByTagName('body')[0].appendChild(printFrame);

    // Get iframe element
    var iframeElement = document.getElementById(params.frameId);

    // If printing pdf in IE
    if (__WEBPACK_IMPORTED_MODULE_0__browser__["a" /* default */].isIE() && params.type === 'pdf') {
      finishPrintPdfIe(iframeElement);
    } else {
      // Wait for iframe to load all content
      printFrame.onload = function () {
        if (params.type === 'pdf') {
          finishPrint(iframeElement, params);
        } else {
          // Get iframe element document
          var printDocument = iframeElement.contentWindow || iframeElement.contentDocument;
          if (printDocument.document) printDocument = printDocument.document;

          // Inject printable html into iframe body
          printDocument.body.innerHTML = params.htmlData;

          // If printing image, wait for it to load inside the iframe (skip firefox)
          if (params.type === 'image') {
            loadImageAndFinishPrint(printDocument.getElementById('printableImage'), iframeElement, params);
          } else {
            finishPrint(iframeElement, params);
          }
        }
      };
    }
  }
};

function finishPrint(iframeElement, params) {
  // Print iframe document
  iframeElement.focus();

  // If IE or Edge, try catch with execCommand
  if (__WEBPACK_IMPORTED_MODULE_0__browser__["a" /* default */].isIE() || __WEBPACK_IMPORTED_MODULE_0__browser__["a" /* default */].isEdge()) {
    try {
      iframeElement.contentWindow.document.execCommand('print', false, null);
    } catch (e) {
      iframeElement.contentWindow.print();
    }
  } else {
    iframeElement.contentWindow.print();
  }

  // If we are showing a feedback message to user, remove it
  if (params.showModal) {
    __WEBPACK_IMPORTED_MODULE_1__modal__["a" /* default */].close();
  }
}

function finishPrintPdfIe(iframeElement) {
  // Wait until pdf is ready to print
  if (typeof iframeElement.print === 'undefined') {
    setTimeout(function () {
      finishPrintPdfIe();
    }, 1000);
  } else {
    Print.send();

    // Remove embed (just because it isn't 100% hidden when using h/w = 0)
    setTimeout(function () {
      iframeElement.parentNode.removeChild(iframeElement);
    }, 2000);
  }
}

function loadImageAndFinishPrint(img, iframeElement, params) {
  if (typeof img.naturalWidth === 'undefined' || img.naturalWidth === 0) {
    setTimeout(function () {
      loadImageAndFinishPrint(img, iframeElement, params);
    }, 500);
  } else {
    finishPrint(iframeElement, params);
  }
}

/* harmony default export */ __webpack_exports__["a"] = (Print);

/***/ }),
/* 1 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
var Browser = {
  // Firefox 1.0+
  isFirefox: function isFirefox() {
    return typeof InstallTrigger !== 'undefined';
  },
  // Internet Explorer 6-11
  isIE: function isIE() {
    return !!document.documentMode;
  },
  // Edge 20+
  isEdge: function isEdge() {
    return !Browser.isIE() && !!window.StyleMedia;
  },
  // Chrome 1+
  isChrome: function isChrome() {
    return !!window.chrome && !!window.chrome.webstore;
  },
  // At least Safari 3+: "[object HTMLElementConstructor]"
  isSafari: function isSafari() {
    return Object.prototype.toString.call(window.HTMLElement).indexOf('Constructor') > 0 || navigator.userAgent.toLowerCase().indexOf('safari') !== -1;
  }
};

/* harmony default export */ __webpack_exports__["a"] = (Browser);

/***/ }),
/* 2 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (immutable) */ __webpack_exports__["b"] = addWrapper;
/* harmony export (immutable) */ __webpack_exports__["c"] = capitalizePrint;
/* harmony export (immutable) */ __webpack_exports__["d"] = collectStyles;
/* harmony export (immutable) */ __webpack_exports__["e"] = loopNodesCollectStyles;
/* harmony export (immutable) */ __webpack_exports__["a"] = addHeader;
function addWrapper(htmlData, params) {
  var bodyStyle = 'font-family:' + params.font + ' !important; font-size: ' + params.font_size + ' !important; width:100%;';
  return '<div style="' + bodyStyle + '">' + htmlData + '</div>';
}

function capitalizePrint(string) {
  return string.charAt(0).toUpperCase() + string.slice(1);
}

function collectStyles(element, params) {
  var win = document.defaultView || window;

  var style = [];

  // String variable to hold styling for each element
  var elementStyle = '';

  if (win.getComputedStyle) {
    // modern browsers
    style = win.getComputedStyle(element, '');

    // Styles including
    var targetStyles = ['border', 'float', 'box', 'break', 'text-decoration'];

    // Exact match
    var targetStyle = ['clear', 'display', 'width', 'min-width', 'height', 'min-height', 'max-height'];

    // Optional - include margin and padding
    if (params.honorMarginPadding) {
      targetStyles.push('margin', 'padding');
    }

    // Optional - include color
    if (params.honorColor) {
      targetStyles.push('color');
    }

    for (var i = 0; i < style.length; i++) {
      for (var s = 0; s < targetStyle.length; s++) {
        if (style[i].indexOf(targetStyles[s]) !== -1 || style[i].indexOf(targetStyle[s]) === 0) {
          elementStyle += style[i] + ':' + style.getPropertyValue(style[i]) + ';';
        }
      }
    }
  } else if (element.currentStyle) {
    // IE
    style = element.currentStyle;

    for (var name in style) {
      if (style.indexOf('border') !== -1 && style.indexOf('color') !== -1) {
        elementStyle += name + ':' + style[name] + ';';
      }
    }
  }

  // Print friendly defaults
  elementStyle += 'max-width: ' + params.maxWidth + 'px !important;' + params.font_size + ' !important;';

  return elementStyle;
}

function loopNodesCollectStyles(elements, params) {
  for (var n = 0; n < elements.length; n++) {
    var currentElement = elements[n];

    // Form Printing - check if is element Input
    var tag = currentElement.tagName;
    if (tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT') {
      // Save style to variable
      var textStyle = collectStyles(currentElement, params);

      // Remove INPUT element and insert a text node
      var parent = currentElement.parentNode;

      // Get text value
      var textNode = tag === 'SELECT' ? document.createTextNode(currentElement.options[currentElement.selectedIndex].text) : document.createTextNode(currentElement.value);

      // Create text element
      var textElement = document.createElement('div');
      textElement.appendChild(textNode);

      // Add style to text
      textElement.setAttribute('style', textStyle);

      // Add text
      parent.appendChild(textElement);

      // Remove input
      parent.removeChild(currentElement);
    } else {
      // Get all styling for print element
      currentElement.setAttribute('style', collectStyles(currentElement, params));
    }

    // Check if more elements in tree
    var children = currentElement.children;

    if (children && children.length) {
      loopNodesCollectStyles(children, params);
    }
  }
}

function addHeader(printElement, header) {
  // Create header element
  var headerElement = document.createElement('h1');

  // Create header text node
  var headerNode = document.createTextNode(header);

  // Build and style
  headerElement.appendChild(headerNode);
  headerElement.setAttribute('style', 'font-weight:300;');

  printElement.insertBefore(headerElement, printElement.childNodes[0]);
}

/***/ }),
/* 3 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
var Modal = {
  show: function show(params) {
    // Build modal
    var modalStyle = 'font-family:sans-serif; ' + 'display:table; ' + 'text-align:center; ' + 'font-weight:300; ' + 'font-size:30px; ' + 'left:0; top:0;' + 'position:fixed; ' + 'z-index: 9990;' + 'color: #0460B5; ' + 'width: 100%; ' + 'height: 100%; ' + 'background-color:rgba(255,255,255,.9);' + 'transition: opacity .3s ease;';

    // Create wrapper
    var printModal = document.createElement('div');
    printModal.setAttribute('style', modalStyle);
    printModal.setAttribute('id', 'printJS-Modal');

    // Create content div
    var contentDiv = document.createElement('div');
    contentDiv.setAttribute('style', 'display:table-cell; vertical-align:middle; padding-bottom:100px;');

    // Add close button (requires print.css)
    var closeButton = document.createElement('div');
    closeButton.setAttribute('class', 'printClose');
    closeButton.setAttribute('id', 'printClose');
    contentDiv.appendChild(closeButton);

    // Add spinner (requires print.css)
    var spinner = document.createElement('span');
    spinner.setAttribute('class', 'printSpinner');
    contentDiv.appendChild(spinner);

    // Add message
    var messageNode = document.createTextNode(params.modalMessage);
    contentDiv.appendChild(messageNode);

    // Add contentDiv to printModal
    printModal.appendChild(contentDiv);

    // Append print modal element to document body
    document.getElementsByTagName('body')[0].appendChild(printModal);

    // Add event listener to close button
    document.getElementById('printClose').addEventListener('click', function () {
      Modal.close();
    });
  },
  close: function close() {
    var printFrame = document.getElementById('printJS-Modal');

    printFrame.parentNode.removeChild(printFrame);
  }
};

/* harmony default export */ __webpack_exports__["a"] = (Modal);

/***/ }),
/* 4 */
/***/ (function(module, exports, __webpack_require__) {

module.exports = __webpack_require__(5);


/***/ }),
/* 5 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
Object.defineProperty(__webpack_exports__, "__esModule", { value: true });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__js_init__ = __webpack_require__(6);


var printjs = __WEBPACK_IMPORTED_MODULE_0__js_init__["a" /* default */].init;

if (typeof window !== 'undefined') {
  window.printJS = printjs;
}

/* harmony default export */ __webpack_exports__["default"] = (printjs);

/***/ }),
/* 6 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__browser__ = __webpack_require__(1);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__modal__ = __webpack_require__(3);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__pdf__ = __webpack_require__(7);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__html__ = __webpack_require__(8);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__image__ = __webpack_require__(9);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5__json__ = __webpack_require__(10);


var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };








var printTypes = ['pdf', 'html', 'image', 'json'];

/* harmony default export */ __webpack_exports__["a"] = ({
  init: function init() {
    var params = {
      printable: null,
      type: 'pdf',
      header: null,
      maxWidth: 800,
      font: 'TimesNewRoman',
      font_size: '12pt',
      honorMarginPadding: true,
      honorColor: false,
      properties: null,
      showModal: false,
      modalMessage: 'Retrieving Document...',
      frameId: 'printJS',
      border: true,
      htmlData: ''

      // Check if a printable document or object was supplied
    };var args = arguments[0];
    if (args === undefined) {
      throw new Error('printJS expects at least 1 attribute.');
    }

    switch (typeof args === 'undefined' ? 'undefined' : _typeof(args)) {
      case 'string':
        params.printable = encodeURI(args);
        params.type = arguments[1] || params.type;
        break;

      case 'object':
        params.printable = args.printable;
        params.type = typeof args.type !== 'undefined' ? args.type : params.type;
        params.frameId = typeof args.frameId !== 'undefined' ? args.frameId : params.frameId;
        params.header = typeof args.header !== 'undefined' ? args.header : params.header;
        params.maxWidth = typeof args.maxWidth !== 'undefined' ? args.maxWidth : params.maxWidth;
        params.font = typeof args.font !== 'undefined' ? args.font : params.font;
        params.font_size = typeof args.font_size !== 'undefined' ? args.font_size : params.font_size;
        params.honorMarginPadding = typeof args.honorMarginPadding !== 'undefined' ? args.honorMarginPadding : params.honorMarginPadding;
        params.properties = typeof args.properties !== 'undefined' ? args.properties : params.properties;
        params.showModal = typeof args.showModal !== 'undefined' ? args.showModal : params.showModal;
        params.modalMessage = typeof args.modalMessage !== 'undefined' ? args.modalMessage : params.modalMessage;
        break;
      default:
        throw new Error('Unexpected argument type! Expected "string" or "object", got ' + (typeof args === 'undefined' ? 'undefined' : _typeof(args)));
    }

    if (!params.printable) {
      throw new Error('Missing printable information.');
    }

    if (!params.type || typeof params.type !== 'string' || printTypes.indexOf(params.type.toLowerCase()) === -1) {
      throw new Error('Invalid print type. Available types are: pdf, html, image and json.');
    }

    // Check if we are showing a feedback message to the user (useful for large files)
    if (params.showModal) {
      __WEBPACK_IMPORTED_MODULE_1__modal__["a" /* default */].show(params);
    }

    // To prevent duplication and issues, remove printFrame from the DOM, if it exists
    var usedFrame = document.getElementById(params.frameId);

    if (usedFrame) {
      usedFrame.parentNode.removeChild(usedFrame);
    }

    // Create a new iframe or embed element (IE prints blank pdf's if we use iframe)
    var printFrame = void 0;

    if (__WEBPACK_IMPORTED_MODULE_0__browser__["a" /* default */].isIE() && params.type === 'pdf') {
      // Create embed element
      printFrame = document.createElement('embed');
      printFrame.setAttribute('type', 'application/pdf');

      // Hide embed
      printFrame.setAttribute('style', 'width:0px;height:0px;');
    } else {
      // Create iframe element
      printFrame = document.createElement('iframe');

      // Hide iframe
      printFrame.setAttribute('style', 'display:none;');
    }

    // Set element id
    printFrame.setAttribute('id', params.frameId);

    // For non pdf printing in Chrome and Safari, pass an empty html document to srcdoc (force onload callback)
    if (params.type !== 'pdf' && (__WEBPACK_IMPORTED_MODULE_0__browser__["a" /* default */].isChrome() || __WEBPACK_IMPORTED_MODULE_0__browser__["a" /* default */].isSafari())) {
      printFrame.srcdoc = '<html><head></head><body></body></html>';
    }

    // Check printable type
    switch (params.type) {
      case 'pdf':
        // Check browser support for pdf and if not supported we will just open the pdf file instead
        if (__WEBPACK_IMPORTED_MODULE_0__browser__["a" /* default */].isFirefox() || __WEBPACK_IMPORTED_MODULE_0__browser__["a" /* default */].isIE() || __WEBPACK_IMPORTED_MODULE_0__browser__["a" /* default */].isEdge()) {
          console.log('PrintJS currently doesn\'t support PDF printing in Firefox, Internet Explorer and Edge.');
          var win = window.open(params.printable, '_blank');
          win.focus();
          // Make sure there is no loading modal opened
          if (params.showModal) __WEBPACK_IMPORTED_MODULE_1__modal__["a" /* default */].close();
        } else {
          __WEBPACK_IMPORTED_MODULE_2__pdf__["a" /* default */].print(params, printFrame);
        }
        break;
      case 'image':
        __WEBPACK_IMPORTED_MODULE_4__image__["a" /* default */].print(params, printFrame);
        break;
      case 'html':
        __WEBPACK_IMPORTED_MODULE_3__html__["a" /* default */].print(params, printFrame);
        break;
      case 'json':
        __WEBPACK_IMPORTED_MODULE_5__json__["a" /* default */].print(params, printFrame);
        break;
      default:
        throw new Error('Invalid print type. Available types are: pdf, html, image and json.');
    }
  }
});

/***/ }),
/* 7 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__browser__ = __webpack_require__(1);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__print__ = __webpack_require__(0);



/* harmony default export */ __webpack_exports__["a"] = ({
  print: function print(params, printFrame) {
    // If showing feedback to user, pre load pdf files (hacky)
    // Since we will be using promises, we can't use this feature in IE
    if (params.showModal && !__WEBPACK_IMPORTED_MODULE_0__browser__["a" /* default */].isIE()) {
      var pdfObject = document.createElement('img');
      pdfObject.src = params.printable;

      var pdf = new Promise(function (resolve, reject) {
        var loadPDF = setInterval(checkPDFload, 100);

        function checkPDFload() {
          if (pdfObject.complete) {
            window.clearInterval(loadPDF);
            resolve('PrintJS: PDF loaded.');
          }
        }
      });

      pdf.then(function (result) {
        send(params, printFrame);
      });
    } else {
      send(params, printFrame);
    }
  }
});

function send(params, printFrame) {
  // Set iframe src with pdf document url
  printFrame.setAttribute('src', params.printable);

  // Print pdf document
  __WEBPACK_IMPORTED_MODULE_1__print__["a" /* default */].send(params, printFrame);
}

/***/ }),
/* 8 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__functions__ = __webpack_require__(2);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__print__ = __webpack_require__(0);



/* harmony default export */ __webpack_exports__["a"] = ({
  print: function print(params, printFrame) {
    // Get HTML printable element
    var printElement = document.getElementById(params.printable);

    // Check if element exists
    if (!printElement) {
      window.console.error('Invalid HTML element id: ' + params.printable);

      return false;
    }

    // Make a copy of the printElement to prevent DOM changes
    var printableElement = document.createElement('div');
    printableElement.appendChild(printElement.cloneNode(true));

    // Add cloned element to DOM, to have DOM element methods available. It will also be easier to colect styles
    printableElement.setAttribute('style', 'display:none;');
    printableElement.setAttribute('id', 'printJS-html');
    printElement.parentNode.appendChild(printableElement);

    // Update printableElement variable with newly created DOM element
    printableElement = document.getElementById('printJS-html');

    // Get main element styling
    printableElement.setAttribute('style', Object(__WEBPACK_IMPORTED_MODULE_0__functions__["d" /* collectStyles */])(printableElement, params) + 'margin:0 !important;');

    // Get all children elements
    var elements = printableElement.children;

    // Get styles for all children elements
    Object(__WEBPACK_IMPORTED_MODULE_0__functions__["e" /* loopNodesCollectStyles */])(elements, params);

    // Add header if any
    if (params.header) {
      Object(__WEBPACK_IMPORTED_MODULE_0__functions__["a" /* addHeader */])(printableElement, params.header);
    }

    // Remove DOM printableElement
    printableElement.parentNode.removeChild(printableElement);

    // Store html data
    params.htmlData = Object(__WEBPACK_IMPORTED_MODULE_0__functions__["b" /* addWrapper */])(printableElement.innerHTML, params);

    // Print html element contents
    __WEBPACK_IMPORTED_MODULE_1__print__["a" /* default */].send(params, printFrame);
  }
});

/***/ }),
/* 9 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__functions__ = __webpack_require__(2);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__print__ = __webpack_require__(0);



/* harmony default export */ __webpack_exports__["a"] = ({
  print: function print(params, printFrame) {
    // Create the image element
    var img = document.createElement('img');

    // Set image src with image file url
    img.src = params.printable;

    // Load image
    img.onload = function () {
      img.setAttribute('style', 'width:100%;');
      img.setAttribute('id', 'printableImage');

      // Create wrapper
      var printableElement = document.createElement('div');
      printableElement.setAttribute('style', 'width:100%');
      printableElement.appendChild(img);

      // Check if we are adding a header for the image
      if (params.header) {
        Object(__WEBPACK_IMPORTED_MODULE_0__functions__["a" /* addHeader */])(printableElement);
      }

      // Store html data
      params.htmlData = printableElement.outerHTML;

      // Print image
      __WEBPACK_IMPORTED_MODULE_1__print__["a" /* default */].send(params, printFrame);
    };
  }
});

/***/ }),
/* 10 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__functions__ = __webpack_require__(2);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__print__ = __webpack_require__(0);
var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };




/* harmony default export */ __webpack_exports__["a"] = ({
  print: function print(params, printFrame) {
    // Check if we received proper data
    if (_typeof(params.printable) !== 'object') {
      throw new Error('Invalid javascript data object (JSON).');
    }

    // Check if properties were provided
    if (!params.properties || _typeof(params.properties) !== 'object') {
      throw new Error('Invalid properties array for your JSON data.');
    }

    // Variable to hold html string
    var htmlData = '';

    // Check print has header
    if (params.header) {
      htmlData += '<h1 style="font-weight:300;">' + params.header + '</h1>';
    }

    // Function to build html templates for json data
    htmlData += jsonToHTML(params);

    // Store html data
    params.htmlData = Object(__WEBPACK_IMPORTED_MODULE_0__functions__["b" /* addWrapper */])(htmlData, params);

    // Print json data
    __WEBPACK_IMPORTED_MODULE_1__print__["a" /* default */].send(params, printFrame);
  }
});

function jsonToHTML(params) {
  var data = params.printable;
  var properties = params.properties;

  var htmlData = '<div style="display:flex; flex-direction: column;">';

  // Header
  htmlData += '<div style="flex:1 1 auto; display:flex;">';

  for (var a = 0; a < properties.length; a++) {
    htmlData += '<div style="flex:1; padding:5px;">' + Object(__WEBPACK_IMPORTED_MODULE_0__functions__["c" /* capitalizePrint */])(properties[a]['displayName'] || properties[a]) + '</div>';
  }

  htmlData += '</div>';

  // Create html data
  for (var i = 0; i < data.length; i++) {
    htmlData += '<div style="flex:1 1 auto; display:flex;';
    htmlData += params.border ? 'border:1px solid lightgray;' : '';
    htmlData += '">';

    for (var n = 0; n < properties.length; n++) {
      htmlData += '<div style="flex:1; padding:5px;">' + data[i][properties[n]['field'] || properties[n]] + '</div>';
    }

    htmlData += '</div>';
  }

  htmlData += '</div>';

  return htmlData;
}

/***/ })
/******/ ]);