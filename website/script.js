/* ============================================
   GymTrack Website — JavaScript
   ============================================ */
(function () {
  'use strict';
  document.addEventListener('DOMContentLoaded', init);

  function init() {
    initParticles();
    initCursor();
    initNavbar();
    initMobileNav();
    initScrollAnimations();
    initTabs();
    initORMCalculator();
    initTimer();
    initPlateCalculator();
    initMagneticButtons();
  }

  /* --- Particles --- */
  function initParticles() {
    var canvas = document.getElementById('particleCanvas');
    if (!canvas) return;
    var ctx = canvas.getContext('2d');
    var particles = [];
    var COUNT = 50;
    var CONN_DIST = 140;

    function resize() { canvas.width = window.innerWidth; canvas.height = window.innerHeight; }
    resize();
    window.addEventListener('resize', resize);

    for (var i = 0; i < COUNT; i++) {
      particles.push({
        x: Math.random() * canvas.width,
        y: Math.random() * canvas.height,
        vx: (Math.random() - 0.5) * 0.35,
        vy: (Math.random() - 0.5) * 0.35,
        r: Math.random() * 1.5 + 0.5,
        o: Math.random() * 0.3 + 0.1
      });
    }

    var mx = -9999, my = -9999;
    document.addEventListener('mousemove', function (e) { mx = e.clientX; my = e.clientY; });

    function draw() {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      for (var i = 0; i < particles.length; i++) {
        var p = particles[i];
        p.x += p.vx; p.y += p.vy;
        if (p.x < 0 || p.x > canvas.width) p.vx *= -1;
        if (p.y < 0 || p.y > canvas.height) p.vy *= -1;
        ctx.beginPath();
        ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
        ctx.fillStyle = 'rgba(0,180,216,' + p.o + ')';
        ctx.fill();

        for (var j = i + 1; j < particles.length; j++) {
          var q = particles[j];
          var dx = p.x - q.x, dy = p.y - q.y;
          var dist = Math.sqrt(dx * dx + dy * dy);
          if (dist < CONN_DIST) {
            ctx.beginPath();
            ctx.moveTo(p.x, p.y);
            ctx.lineTo(q.x, q.y);
            ctx.strokeStyle = 'rgba(0,180,216,' + ((1 - dist / CONN_DIST) * 0.12) + ')';
            ctx.lineWidth = 0.5;
            ctx.stroke();
          }
        }

        var mdx = p.x - mx, mdy = p.y - my;
        var mdist = Math.sqrt(mdx * mdx + mdy * mdy);
        if (mdist < 180) {
          ctx.beginPath();
          ctx.moveTo(p.x, p.y);
          ctx.lineTo(mx, my);
          ctx.strokeStyle = 'rgba(0,180,216,' + ((1 - mdist / 180) * 0.18) + ')';
          ctx.lineWidth = 0.5;
          ctx.stroke();
        }
      }
      requestAnimationFrame(draw);
    }
    draw();
  }

  /* --- Cursor --- */
  function initCursor() {
    var dot = document.getElementById('cursorDot');
    var out = document.getElementById('cursorOutline');
    if (!dot || !out || 'ontouchstart' in window) return;
    var dx = 0, dy = 0, ox = 0, oy = 0;
    document.addEventListener('mousemove', function (e) { dx = e.clientX; dy = e.clientY; });
    function tick() {
      dot.style.transform = 'translate(' + (dx - 4) + 'px,' + (dy - 4) + 'px)';
      ox += (dx - ox) * 0.12;
      oy += (dy - oy) * 0.12;
      out.style.left = ox + 'px';
      out.style.top = oy + 'px';
      requestAnimationFrame(tick);
    }
    tick();
    var els = document.querySelectorAll('a,button,.tool-tab,.timer-preset,.bar-option,.tool-btn,.mini-phone-frame');
    els.forEach(function (el) {
      el.addEventListener('mouseenter', function () { out.classList.add('hover'); });
      el.addEventListener('mouseleave', function () { out.classList.remove('hover'); });
    });
  }

  /* --- Navbar --- */
  function initNavbar() {
    var nav = document.getElementById('navbar');
    if (!nav) return;
    window.addEventListener('scroll', function () {
      nav.classList.toggle('scrolled', window.pageYOffset > 50);
    });
  }

  /* --- Mobile Nav --- */
  function initMobileNav() {
    var tog = document.getElementById('navToggle');
    var links = document.getElementById('navLinks');
    if (!tog || !links) return;
    tog.addEventListener('click', function () {
      tog.classList.toggle('active');
      links.classList.toggle('open');
    });
    links.querySelectorAll('.nav-link').forEach(function (l) {
      l.addEventListener('click', function () {
        tog.classList.remove('active');
        links.classList.remove('open');
      });
    });
  }

  /* --- Scroll Animations --- */
  function initScrollAnimations() {
    var els = document.querySelectorAll('.animate-on-scroll');
    if (!els.length) return;
    var obs = new IntersectionObserver(function (entries) {
      entries.forEach(function (e) {
        if (e.isIntersecting) e.target.classList.add('visible');
      });
    }, { threshold: 0.15, rootMargin: '0px 0px -40px 0px' });
    els.forEach(function (el) { obs.observe(el); });
  }

  /* --- Smooth scroll --- */
  document.addEventListener('click', function (e) {
    var a = e.target.closest('a[href^="#"]');
    if (!a || a.getAttribute('href') === '#') return;
    var t = document.querySelector(a.getAttribute('href'));
    if (t) { e.preventDefault(); t.scrollIntoView({ behavior: 'smooth', block: 'start' }); }
  });

  /* ============================================
     Tab Switching
     ============================================ */
  function initTabs() {
    var tabs = document.querySelectorAll('.tool-tab');
    var panels = document.querySelectorAll('.tool-panel');
    tabs.forEach(function (tab) {
      tab.addEventListener('click', function () {
        tabs.forEach(function (t) { t.classList.remove('active'); });
        panels.forEach(function (p) { p.classList.remove('active'); });
        tab.classList.add('active');
        var target = document.getElementById('panel-' + tab.getAttribute('data-tab'));
        if (target) target.classList.add('active');
      });
    });
  }

  /* ============================================
     1RM Calculator
     ============================================ */
  function initORMCalculator() {
    var btn = document.getElementById('calc-orm');
    if (!btn) return;
    btn.addEventListener('click', calcORM);
    var wInput = document.getElementById('orm-weight');
    var rInput = document.getElementById('orm-reps');
    if (wInput) wInput.addEventListener('input', calcORM);
    if (rInput) rInput.addEventListener('input', calcORM);
  }

  function calcORM() {
    var weight = parseFloat(document.getElementById('orm-weight').value);
    var reps = parseFloat(document.getElementById('orm-reps').value);
    if (!weight || !reps || weight <= 0 || reps <= 0) return;

    // Epley formula
    var orm = reps === 1 ? weight : Math.round(weight * (1 + reps / 30));

    document.getElementById('orm-result-value').textContent = orm;
    document.getElementById('orm-90').textContent = Math.round(orm * 0.9) + ' kg';
    document.getElementById('orm-85').textContent = Math.round(orm * 0.85) + ' kg';
    document.getElementById('orm-80').textContent = Math.round(orm * 0.8) + ' kg';
    document.getElementById('orm-75').textContent = Math.round(orm * 0.75) + ' kg';
    document.getElementById('orm-70').textContent = Math.round(orm * 0.7) + ' kg';
    document.getElementById('orm-65').textContent = Math.round(orm * 0.65) + ' kg';
    document.getElementById('orm-60').textContent = Math.round(orm * 0.6) + ' kg';
    document.getElementById('orm-50').textContent = Math.round(orm * 0.5) + ' kg';

    var tip = '';
    if (reps <= 3) tip = 'Low rep range — great for testing pure strength.';
    else if (reps <= 6) tip = 'Classic strength rep range. Use 80-90% for your working sets.';
    else if (reps <= 12) tip = 'Hypertrophy zone. Use 65-75% for muscle growth.';
    else tip = 'High rep endurance work. Consider lowering weight for next session.';
    document.getElementById('orm-tip').textContent = tip;
  }

  /* ============================================
     Rest Timer
     ============================================ */
  var timerInterval = null;
  var timerSeconds = 60;
  var timerRemaining = 60;
  var timerRunning = false;

  function initTimer() {
    var presets = document.querySelectorAll('.timer-preset');
    var startBtn = document.getElementById('timer-start');
    var resetBtn = document.getElementById('timer-reset');

    presets.forEach(function (p) {
      p.addEventListener('click', function () {
        if (timerRunning) return;
        presets.forEach(function (x) { x.classList.remove('active'); });
        p.classList.add('active');
        timerSeconds = parseInt(p.getAttribute('data-seconds'));
        timerRemaining = timerSeconds;
        updateTimerDisplay();
      });
    });

    if (startBtn) startBtn.addEventListener('click', toggleTimer);
    if (resetBtn) resetBtn.addEventListener('click', resetTimer);
    updateTimerDisplay();
  }

  function toggleTimer() {
    if (timerRunning) {
      pauseTimer();
    } else {
      startTimer();
    }
  }

  function startTimer() {
    if (timerRemaining <= 0) {
      timerRemaining = timerSeconds;
    }
    timerRunning = true;
    var btn = document.getElementById('timer-start');
    btn.innerHTML = '<i class="fas fa-pause"></i> Pause';
    var label = document.getElementById('timer-status');
    label.textContent = 'Running';
    var fill = document.getElementById('timer-ring-fill');
    fill.classList.add('running');
    fill.classList.remove('done');

    timerInterval = setInterval(function () {
      timerRemaining--;
      updateTimerDisplay();
      if (timerRemaining <= 0) {
        clearInterval(timerInterval);
        timerRunning = false;
        btn.innerHTML = '<i class="fas fa-play"></i> Start';
        label.textContent = 'Done!';
        fill.classList.remove('running');
        fill.classList.add('done');
      }
    }, 1000);
  }

  function pauseTimer() {
    clearInterval(timerInterval);
    timerRunning = false;
    var btn = document.getElementById('timer-start');
    btn.innerHTML = '<i class="fas fa-play"></i> Resume';
    document.getElementById('timer-status').textContent = 'Paused';
  }

  function resetTimer() {
    clearInterval(timerInterval);
    timerRunning = false;
    timerRemaining = timerSeconds;
    var btn = document.getElementById('timer-start');
    btn.innerHTML = '<i class="fas fa-play"></i> Start';
    document.getElementById('timer-status').textContent = 'Ready';
    var fill = document.getElementById('timer-ring-fill');
    fill.classList.remove('running', 'done');
    updateTimerDisplay();
  }

  function updateTimerDisplay() {
    var min = Math.floor(timerRemaining / 60);
    var sec = timerRemaining % 60;
    document.getElementById('timer-display').textContent = min + ':' + (sec < 10 ? '0' : '') + sec;

    // Update ring
    var circumference = 2 * Math.PI * 90; // r=90
    var fill = document.getElementById('timer-ring-fill');
    var progress = timerSeconds > 0 ? (timerSeconds - timerRemaining) / timerSeconds : 0;
    fill.style.strokeDashoffset = circumference * (1 - progress);
  }

  /* ============================================
     Plate Calculator
     ============================================ */
  function initPlateCalculator() {
    var btn = document.getElementById('calc-plates');
    if (!btn) return;

    // Bar options
    var barOpts = document.querySelectorAll('.bar-option');
    barOpts.forEach(function (opt) {
      opt.addEventListener('click', function () {
        barOpts.forEach(function (o) { o.classList.remove('active'); });
        opt.classList.add('active');
      });
    });

    btn.addEventListener('click', calcPlates);
    var wInput = document.getElementById('plate-weight');
    if (wInput) wInput.addEventListener('input', calcPlates);
  }

  function calcPlates() {
    var targetWeight = parseFloat(document.getElementById('plate-weight').value);
    var barEl = document.querySelector('.bar-option.active');
    var barWeight = barEl ? parseFloat(barEl.getAttribute('data-bar')) : 20;

    if (!targetWeight || targetWeight < barWeight) {
      document.getElementById('plate-per-side').textContent = '—';
      document.getElementById('plate-side-left').innerHTML = '';
      document.getElementById('plate-side-right').innerHTML = '';
      document.getElementById('plate-breakdown').innerHTML = '';
      document.getElementById('plate-tip').textContent = 'Weight must be at least ' + barWeight + 'kg (bar weight)';
      return;
    }

    var perSide = (targetWeight - barWeight) / 2;
    document.getElementById('plate-per-side').textContent = perSide.toFixed(1);

    var plates = [25, 20, 15, 10, 5, 2.5, 1.25];
    var plateClasses = ['p-25', 'p-20', 'p-15', 'p-10', 'p-5', 'p-2_5', 'p-1_25'];
    var remaining = perSide;
    var used = [];

    for (var i = 0; i < plates.length; i++) {
      var count = Math.floor(remaining / plates[i]);
      if (count > 0) {
        used.push({ weight: plates[i], count: count, cls: plateClasses[i] });
        remaining = Math.round((remaining - plates[i] * count) * 100) / 100;
      }
    }

    // Visual
    var leftHTML = '';
    var rightHTML = '';
    used.forEach(function (u) {
      for (var c = 0; c < u.count; c++) {
        leftHTML += '<div class="plate-disc ' + u.cls + '"></div>';
        rightHTML += '<div class="plate-disc ' + u.cls + '"></div>';
      }
    });
    document.getElementById('plate-side-left').innerHTML = leftHTML;
    document.getElementById('plate-side-right').innerHTML = rightHTML;

    // Breakdown
    var bHTML = '';
    used.forEach(function (u) {
      bHTML += '<div class="plate-tag">' + u.count + 'x ' + u.weight + 'kg</div>';
    });
    document.getElementById('plate-breakdown').innerHTML = bHTML;

    // Tip
    document.getElementById('plate-tip').textContent =
      'Load ' + perSide.toFixed(1) + 'kg per side using ' + barWeight + 'kg bar. Total: ' + targetWeight + 'kg.';
  }

  /* ============================================
     Magnetic Buttons
     ============================================ */
  function initMagneticButtons() {
    if ('ontouchstart' in window) return;
    document.querySelectorAll('.magnetic-btn').forEach(function (btn) {
      btn.addEventListener('mousemove', function (e) {
        var r = btn.getBoundingClientRect();
        var x = e.clientX - r.left - r.width / 2;
        var y = e.clientY - r.top - r.height / 2;
        btn.style.transform = 'translate(' + (x * 0.15) + 'px,' + (y * 0.15) + 'px)';
      });
      btn.addEventListener('mouseleave', function () {
        btn.style.transform = 'translate(0,0)';
      });
    });
  }
})();
