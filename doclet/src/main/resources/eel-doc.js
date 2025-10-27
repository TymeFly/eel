"use strict";

  window.onscroll = function() {
    document.getElementById("scroll-up").style.display =
        (document.body.scrollTop > 80 || document.documentElement.scrollTop > 80) ? "block" : "none";
  }

