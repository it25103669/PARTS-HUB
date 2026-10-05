/**
 * PartsHub Enterprise — Auth Manager
 * =====================================
 * Prototype & Production-Aligned Implementation:
 *   - Exactly 6 stakeholder accounts, one per team member / module owner.
 *   - Passwords stored exclusively as SHA-256 cryptographic hashes.
 *   - In Spring Boot: Replaced by Spring Security BCryptPasswordEncoder
 *     and UserDetailsService querying the MS SQL Server user table.
 *
 * Storage  : sessionStorage key "ph_session"
 * Shape    : { role, name, initials, title, sessionLabel, landingPage,
 *               nav, badge }
 */

(function (PH) {
  "use strict";

  if (!String.prototype.trim) {
    String.prototype.trim = function () {
      return this.replace(/^\s+|\s+$/g, "");
    };
  }

  /* ─────────────────────────────────────────────────────────────
   * SHA-256 HASH ENGINE
   * Deterministic client-side cryptographic hasher for prototype.
   * Mirrors backend password hashing (e.g. BCrypt / SHA-256).
   * ───────────────────────────────────────────────────────────── */
  function sha256(ascii) {
    function rightRotate(value, amount) {
      return (value >>> amount) | (value << (32 - amount));
    }
    var mathPow = Math.pow;
    var maxWord = mathPow(2, 32);
    var lengthProperty = "length";
    var i, j;
    var result = "";
    var words = [];
    var asciiBitLength = ascii[lengthProperty] * 8;
    var hash = [];
    var k = [];
    var primeCounter = 0;
    var isComposite = {};
    for (var candidate = 2; primeCounter < 64; candidate++) {
      if (!isComposite[candidate]) {
        for (i = 0; i < 313; i += candidate) {
          isComposite[i] = candidate;
        }
        hash[primeCounter] = (mathPow(candidate, 0.5) * maxWord) | 0;
        k[primeCounter++] = (mathPow(candidate, 1 / 3) * maxWord) | 0;
      }
    }
    ascii += "\x80";
    while ((ascii[lengthProperty] % 64) - 56) ascii += "\x00";
    for (i = 0; i < ascii[lengthProperty]; i++) {
      j = ascii.charCodeAt(i);
      if (j >> 8) return "";
      words[i >> 2] |= j << (((3 - i) % 4) * 8);
    }
    words[words[lengthProperty]] = (asciiBitLength / maxWord) | 0;
    words[words[lengthProperty]] = asciiBitLength;
    for (j = 0; j < words[lengthProperty]; ) {
      var w = words.slice(j, (j += 16));
      var oldHash = hash;
      hash = hash.slice(0, 8);
      for (i = 0; i < 64; i++) {
        var w15 = w[i - 15], w2 = w[i - 2];
        var s0 = rightRotate(w15, 7) ^ rightRotate(w15, 18) ^ (w15 >>> 3);
        var s1 = rightRotate(w2, 17) ^ rightRotate(w2, 19) ^ (w2 >>> 10);
        var ch = (hash[4] & hash[5]) ^ (~hash[4] & hash[6]);
        var temp1 =
          hash[7] +
          (rightRotate(hash[4], 6) ^ rightRotate(hash[4], 11) ^ rightRotate(hash[4], 25)) +
          ch +
          k[i] +
          (w[i] =
            i < 16
              ? w[i]
              : (w[i - 16] + s0 + w[i - 7] + s1) | 0);
        var maj = (hash[0] & hash[1]) ^ (hash[0] & hash[2]) ^ (hash[1] & hash[2]);
        var temp2 =
          (rightRotate(hash[0], 2) ^ rightRotate(hash[0], 13) ^ rightRotate(hash[0], 22)) +
          maj;
        hash = [(temp1 + temp2) | 0].concat(hash);
        hash[4] = (hash[4] + temp1) | 0;
      }
      for (i = 0; i < 8; i++) {
        hash[i] = (hash[i] + oldHash[i]) | 0;
      }
    }
    for (i = 0; i < 8; i++) {
      for (j = 3; j >= 0; j--) {
        var b = (hash[i] >> (j * 8)) & 255;
        result += (b < 16 ? "0" : "") + b.toString(16);
      }
    }
    return result;
  }

  PH.sha256 = sha256;

  /* ─────────────────────────────────────────────────────────────
   * 1. ROLE REGISTRY (Exactly 6 Module-Owning Roles)
   *    Each role defines: display info, landing page, which sidebar
   *    nav modules are allowed, and avatar badge styling.
   * ───────────────────────────────────────────────────────────── */
  var ALL_NAV = ["dashboard", "inventory", "suppliers", "purchases", "sales", "returns"];

  var ROLES = {
    // 1. Inventory Management: Perera K.P.S.C.
    ROLE_STOREKEEPER: {
      name: "Perera K.P.S.C.",
      studentId: "IT25102622",
      fullName: "Perera K.P.S.C. (IT25102622)",
      initials: "KP",
      title: "Inventory Management Officer",
      sessionLabel: "Inventory Terminal Live \u2022 IT25102622",
      landingPage: "inventory.html",
      nav: ALL_NAV,
      badge: { color: "#10B981", label: "Inventory Mgt" }
    },

    // 2. Supplier Management: Yuresh D.D.A.
    ROLE_PROCUREMENT: {
      name: "Yuresh D.D.A.",
      studentId: "IT25103669",
      fullName: "Yuresh D.D.A. (IT25103669)",
      initials: "YD",
      title: "Supplier Management Officer",
      sessionLabel: "Procurement Terminal Live \u2022 IT25103669",
      landingPage: "supplier-ranking.html",
      nav: ALL_NAV,
      badge: { color: "#F59E0B", label: "Supplier Mgt" }
    },

    // 3. Purchase and Receiving Management: De Silva T.S.A.I.A
    ROLE_PURCHASING: {
      name: "De Silva T.S.A.I.A",
      studentId: "IT25101669",
      fullName: "De Silva T.S.A.I.A (IT25101669)",
      initials: "TS",
      title: "Purchase & Receiving Manager",
      sessionLabel: "Purchasing Terminal Live \u2022 IT25101669",
      landingPage: "purchases.html",
      nav: ALL_NAV,
      badge: { color: "#3B82F6", label: "Purchase & Receiving" }
    },

    // 4. Sales & Billing Management: Thamilalagan N.
    ROLE_SALES: {
      name: "Thamilalagan N.",
      studentId: "IT25100888",
      fullName: "Thamilalagan N. (IT25100888)",
      initials: "TN",
      title: "Sales & Billing Representative",
      sessionLabel: "Sales Terminal Live \u2022 IT25100888",
      landingPage: "sales.html",
      nav: ALL_NAV,
      badge: { color: "#8B5CF6", label: "Sales & Billing" }
    },

    // 5. Reporting & Executive Dashboard: Indrathilake H.W.S.M
    ROLE_OWNER: {
      name: "Indrathilake H.W.S.M",
      studentId: "IT25100755",
      fullName: "Indrathilake H.W.S.M (IT25100755)",
      initials: "HW",
      title: "Executive Business Owner",
      sessionLabel: "Executive Terminal Live \u2022 IT25100755",
      landingPage: "dashboard.html",
      nav: ALL_NAV,
      badge: { color: "#F59E0B", label: "Executive Owner" }
    },

    // 6. Returns & Warranty Management: Premachandra T.G.D.T
    ROLE_RETURNS: {
      name: "Premachandra T.G.D.T",
      studentId: "IT25101790",
      fullName: "Premachandra T.G.D.T (IT25101790)",
      initials: "TG",
      title: "Returns & Warranty Officer",
      sessionLabel: "Returns Terminal Live \u2022 IT25101790",
      landingPage: "returns.html",
      nav: ALL_NAV,
      badge: { color: "#EC4899", label: "Returns & Warranty" }
    }
  };

  // Add alias keys to ROLES so canonical role names resolve properly
  ROLES.ROLE_PURCHASING_AGENT = ROLES.ROLE_PURCHASING;
  ROLES.PURCHASING_AGENT = ROLES.ROLE_PURCHASING;
  ROLES.PURCHASING = ROLES.ROLE_PURCHASING;
  ROLES.ROLE_PROCUREMENT_OFFICER = ROLES.ROLE_PROCUREMENT;
  ROLES.PROCUREMENT_OFFICER = ROLES.ROLE_PROCUREMENT;
  ROLES.PROCUREMENT = ROLES.ROLE_PROCUREMENT;
  ROLES.ROLE_SALES_REP = ROLES.ROLE_SALES;
  ROLES.SALES_REP = ROLES.ROLE_SALES;
  ROLES.SALES = ROLES.ROLE_SALES;
  ROLES.ROLE_RETURNS_CLERK = ROLES.ROLE_RETURNS;
  ROLES.RETURNS_CLERK = ROLES.ROLE_RETURNS;
  ROLES.RETURNS = ROLES.ROLE_RETURNS;
  ROLES.ROLE_BUSINESS_OWNER = ROLES.ROLE_OWNER;
  ROLES.BUSINESS_OWNER = ROLES.ROLE_OWNER;

  /* ─────────────────────────────────────────────────────────────
   * 2. MODULE REGISTRY
   *    Defines the label, icon, and href for every sidebar item.
   * ───────────────────────────────────────────────────────────── */
  var MODULES = {
    dashboard: { label: "Dashboard & Reports",  icon: "fa-chart-pie",     href: "dashboard.html",        badge: null },
    inventory: { label: "Inventory Management", icon: "fa-boxes-stacked", href: "inventory.html",        badge: "1.4k" },
    suppliers: { label: "Supplier Management",  icon: "fa-truck-field",   href: "supplier-ranking.html", badge: "24" },
    purchases: { label: "Purchase & Receiving", icon: "fa-cart-flatbed",  href: "purchases.html",        badge: null },
    sales:     { label: "Sales & Billing",      icon: "fa-receipt",       href: "sales.html",            badge: null },
    returns:   { label: "Returns & Warranty",   icon: "fa-shield-halved", href: "returns.html",          badge: null }
  };

  /* ─────────────────────────────────────────────────────────────
   * 3. CREDENTIAL TABLE — EXACTLY 6 STAKEHOLDER ACCOUNTS
   *
   *    Seed accounts:
   *      1. perera        (Storekeeper: Inventory)       password: perera123
   *      2. yuresh        (Procurement Officer: Supplier) password: yuresh123
   *      3. desilva       (Purchasing Agent: Purchase)   password: desilva123
   *      4. thamilalagan  (Sales Representative: Sales)  password: thamilalagan123
   *      5. indrathilake  (Business Owner: Dashboard)    password: indrathilake123
   *      6. premachandra  (Returns Clerk: Warranty)      password: premachandra123
   *
   *    Passwords stored strictly as SHA-256 cryptographic hashes.
   *    Plaintext passwords are NEVER stored.
   * ───────────────────────────────────────────────────────────── */
  var CREDENTIALS = {
    // 1. Storekeeper: Perera K.P.S.C. -> password: perera123
    "perera": {
      role: "ROLE_STOREKEEPER",
      password: "perera123",
      passwordHash: "1ad150cbba1393d7a5c1a354d6e1ca7883c1a86a8ac7e0200b7ebf8670a97820"
    },
    "it25102622": {
      role: "ROLE_STOREKEEPER",
      password: "perera123",
      passwordHash: "1ad150cbba1393d7a5c1a354d6e1ca7883c1a86a8ac7e0200b7ebf8670a97820"
    },

    // 2. Procurement Officer: Yuresh D.D.A. -> password: yuresh123
    "yuresh": {
      role: "ROLE_PROCUREMENT",
      password: "yuresh123",
      passwordHash: "6cc135c61374518ffb592f420ae50c90a8b4b6bd37515b5782d3f8c4e9984cf6"
    },
    "it25103669": {
      role: "ROLE_PROCUREMENT",
      password: "yuresh123",
      passwordHash: "6cc135c61374518ffb592f420ae50c90a8b4b6bd37515b5782d3f8c4e9984cf6"
    },

    // 3. Purchasing Agent / Buyer: De Silva T.S.A.I.A -> password: desilva123
    "desilva": {
      role: "ROLE_PURCHASING",
      password: "desilva123",
      passwordHash: "7d1514cb07211bc80c85193b760fb65a504345d4bd63b097f6eb53b3de588fe8"
    },
    "it25101669": {
      role: "ROLE_PURCHASING",
      password: "desilva123",
      passwordHash: "7d1514cb07211bc80c85193b760fb65a504345d4bd63b097f6eb53b3de588fe8"
    },

    // 4. Sales Representative: Thamilalagan N. -> password: thamilalagan123
    "thamilalagan": {
      role: "ROLE_SALES",
      password: "thamilalagan123",
      passwordHash: "edc3613735a84f57cafee441cfc4332de2b6827167c997447552065fdda501ac"
    },
    "it25100888": {
      role: "ROLE_SALES",
      password: "thamilalagan123",
      passwordHash: "edc3613735a84f57cafee441cfc4332de2b6827167c997447552065fdda501ac"
    },

    // 5. Business Owner: Indrathilake H.W.S.M -> password: indrathilake123
    "indrathilake": {
      role: "ROLE_OWNER",
      password: "indrathilake123",
      passwordHash: "9b693d389400f2746ab7d409909ef394ec7656bf5f5fec73d7a2fd27e4063a74"
    },
    "it25100755": {
      role: "ROLE_OWNER",
      password: "indrathilake123",
      passwordHash: "9b693d389400f2746ab7d409909ef394ec7656bf5f5fec73d7a2fd27e4063a74"
    },

    // 6. Returns Clerk: Premachandra T.G.D.T -> password: premachandra123
    "premachandra": {
      role: "ROLE_RETURNS",
      password: "premachandra123",
      passwordHash: "b050cd90ef19109988d4bd8f61d063815bd5f1156178ab6d05ecea988ee6d48e"
    },
    "it25101790": {
      role: "ROLE_RETURNS",
      password: "premachandra123",
      passwordHash: "b050cd90ef19109988d4bd8f61d063815bd5f1156178ab6d05ecea988ee6d48e"
    }
  };

  /* ─────────────────────────────────────────────────────────────
   * 4. PUBLIC API
   * ───────────────────────────────────────────────────────────── */

  /**
   * Validates credentials against records and returns matched role key, or null.
   *
   * @param  {string} username  — trimmed, lowercased
   * @param  {string} password  — raw value from input
   * @returns {string|null}      — e.g. "ROLE_PROCUREMENT" or null
   */
  PH.authenticate = function (username, password) {
    var key = (username || "").trim().toLowerCase();
    var record = CREDENTIALS[key];
    if (record && password) {
      if (record.password === password) {
        return record.role;
      }
      try {
        var computedHash = sha256(password);
        if (computedHash === record.passwordHash) {
          return record.role;
        }
      } catch (e) {}
    }
    return null;
  };

  /**
   * Persists a role session to both sessionStorage and localStorage.
   * @param {string} roleKey  e.g. "ROLE_PROCUREMENT"
   */
  PH.saveSession = function (session) {
    try { sessionStorage.setItem("ph_session", JSON.stringify(session)); } catch (e) {}
    try { localStorage.setItem("ph_session", JSON.stringify(session)); } catch (e) {}
  };

  PH.login = function (roleKey) {
    var cfg = ROLES[roleKey];
    if (!cfg) return false;
    var session = {
      role:         roleKey,
      name:         cfg.name,
      studentId:    cfg.studentId,
      fullName:     cfg.fullName,
      initials:     cfg.initials,
      title:        cfg.title,
      sessionLabel: cfg.sessionLabel,
      landingPage:  cfg.landingPage,
      nav:          cfg.nav,
      badge:        cfg.badge
    };
    PH.saveSession(session);
    return true;
  };

  /** Clears the session and redirects to the login screen. */
  PH.logout = function () {
    try { sessionStorage.removeItem("ph_session"); } catch (e) {}
    try { localStorage.removeItem("ph_session"); } catch (e) {}
    window.location.href = "login.html";
  };

  /** Returns the parsed session object, or null if not logged in. */
  PH.getSession = function () {
    try {
      var s = sessionStorage.getItem("ph_session");
      if (s) return JSON.parse(s);
    } catch (e) {}
    try {
      var l = localStorage.getItem("ph_session");
      if (l) return JSON.parse(l);
    } catch (e) {}
    return null;
  };

  PH.isLoggedIn = function () {
    return !!PH.getSession();
  };

  /**
   * Page guard — redirects to login if no valid session exists.
   * @returns {Object|null} session object, or null (redirect already fired)
   */
  PH.requireAuth = function () {
    if (!PH.isLoggedIn()) {
      window.location.replace("login.html");
      return null;
    }
    return PH.getSession();
  };

  /**
   * Module guard — allows full navigation for authenticated users.
   * @param {string} moduleKey  e.g. "inventory"
   * @returns {Object|null} session object, or null
   */
  PH.requireModule = function (moduleKey) {
    var session = PH.requireAuth();
    if (!session) return null;
    return session;
  };

  /* ─────────────────────────────────────────────────────────────
   * 5. DOM RENDERERS
   * ───────────────────────────────────────────────────────────── */

  /**
   * Builds the sidebar nav from the session's allowed modules.
   * Writes into #sidebar-menu-dynamic.
   * @param {string}   activeModuleKey  — current page's module key
   * @param {string[]} allowedNav       — session.nav array
   */
  PH.renderSidebarNav = function (activeModuleKey, allowedNav) {
    var container = document.getElementById("sidebar-menu-dynamic");
    if (!container) return;

    var html = allowedNav.map(function (key) {
      var m = MODULES[key];
      if (!m) return "";
      var active = key === activeModuleKey ? " active" : "";
      return '<li class="nav-item' + active + '">' +
        '<a href="' + m.href + '" class="nav-link">' +
        '<i class="fa-solid ' + m.icon + '"></i>' +
        '<span class="nav-text">' + m.label + '</span>' +
        (m.badge ? '<span class="nav-badge">' + m.badge + '</span>' : '') +
        '</a></li>';
    }).join("");

    container.innerHTML = html;
  };

  /**
   * Fills the topbar user widget with session data.
   * Targets: #topbar-session-label, #topbar-user-initials,
   *          #topbar-user-name, #topbar-user-role, #topbar-role-badge
   */
  PH.renderTopbar = function (session) {
    var set = function (id, val) {
      var el = document.getElementById(id);
      if (el) el.textContent = val;
    };

    set("topbar-session-label", session.sessionLabel);
    set("topbar-user-initials", session.initials);
    set("topbar-user-name",     session.name + (session.studentId ? " (" + session.studentId + ")" : ""));
    set("topbar-user-role",     session.title);

    var badge = document.getElementById("topbar-role-badge");
    if (badge) {
      badge.textContent            = session.badge.label;
      badge.style.color            = session.badge.color;
      badge.style.borderColor      = session.badge.color;
      badge.style.backgroundColor  = session.badge.color + "1a";
      badge.style.display          = "inline-block";
    }
  };

  /* ─────────────────────────────────────────────────────────────
   * 6. ENTERPRISE LOGOUT SYSTEM & MODAL
   * ───────────────────────────────────────────────────────────── */
  PH.openLogoutModal = function () {
    var modal = document.getElementById("ph-logout-modal");
    if (modal) {
      modal.classList.add("ph-modal-active");
      document.body.style.overflow = "hidden";
    } else {
      PH.logout();
    }
  };

  PH.closeLogoutModal = function () {
    var modal = document.getElementById("ph-logout-modal");
    if (modal) {
      modal.classList.remove("ph-modal-active");
      document.body.style.overflow = "";
    }
  };

  PH.setupLogoutModal = function (session) {
    if (document.getElementById("ph-logout-modal")) return;

    // Inject styles if not present
    var styleId = "ph-logout-styles";
    if (!document.getElementById(styleId)) {
      var style = document.createElement("style");
      style.id = styleId;
      style.textContent = [
        ".btn-topbar-logout {",
        "  display: inline-flex; align-items: center; gap: 7px; padding: 6px 14px;",
        "  background: rgba(239, 68, 68, 0.08); border: 1px solid rgba(239, 68, 68, 0.28);",
        "  border-radius: 8px; color: #F87171; font-size: 12px; font-weight: 600;",
        "  cursor: pointer; transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);",
        "  text-decoration: none; font-family: inherit;",
        "}",
        ".btn-topbar-logout:hover {",
        "  background: rgba(239, 68, 68, 0.2); border-color: #EF4444; color: #FFFFFF;",
        "  box-shadow: 0 0 14px rgba(239, 68, 68, 0.3); transform: translateY(-1px);",
        "}",
        ".btn-topbar-logout:active { transform: translateY(0); }",
        ".sidebar-logout-btn {",
        "  width: 100%; display: flex; align-items: center; justify-content: center;",
        "  gap: 8px; padding: 9px 14px; margin-top: 10px;",
        "  background: rgba(239, 68, 68, 0.08); border: 1px solid rgba(239, 68, 68, 0.22);",
        "  border-radius: 8px; color: #F87171; font-size: 12px; font-weight: 600;",
        "  cursor: pointer; transition: all 0.2s ease; font-family: inherit;",
        "}",
        ".sidebar-logout-btn:hover {",
        "  background: #EF4444; border-color: #EF4444; color: #FFFFFF;",
        "  box-shadow: 0 4px 14px rgba(239, 68, 68, 0.35); transform: translateY(-1px);",
        "}",
        ".sidebar-logout-btn:active { transform: translateY(0); }",
        ".ph-modal-backdrop {",
        "  position: fixed; inset: 0; background: rgba(0, 0, 0, 0.75);",
        "  backdrop-filter: blur(8px); -webkit-backdrop-filter: blur(8px);",
        "  display: flex; align-items: center; justify-content: center; z-index: 99999;",
        "  opacity: 0; pointer-events: none; transition: opacity 0.25s ease;",
        "}",
        ".ph-modal-backdrop.ph-modal-active { opacity: 1; pointer-events: auto; }",
        ".ph-modal-card {",
        "  background: #161A20; border: 1px solid rgba(239, 68, 68, 0.35);",
        "  border-radius: 14px; padding: 26px; width: 100%; max-width: 430px; margin: 16px;",
        "  box-shadow: 0 24px 50px rgba(0, 0, 0, 0.75), 0 0 25px rgba(239, 68, 68, 0.15);",
        "  transform: scale(0.94); transition: transform 0.25s cubic-bezier(0.16, 1, 0.3, 1);",
        "  color: #E2E8F0; font-family: 'Inter', system-ui, sans-serif;",
        "}",
        ".ph-modal-backdrop.ph-modal-active .ph-modal-card { transform: scale(1); }",
        ".ph-modal-header { display: flex; align-items: flex-start; gap: 14px; margin-bottom: 16px; }",
        ".ph-modal-icon-box {",
        "  width: 44px; height: 44px; border-radius: 10px; flex-shrink: 0;",
        "  background: rgba(239, 68, 68, 0.12); border: 1px solid rgba(239, 68, 68, 0.3);",
        "  color: #EF4444; display: flex; align-items: center; justify-content: center; font-size: 19px;",
        "}",
        ".ph-modal-title { font-size: 16px; font-weight: 700; color: #FFFFFF; margin: 0 0 4px 0; }",
        ".ph-modal-subtitle { font-size: 12.5px; color: #94A3B8; margin: 0; line-height: 1.5; }",
        ".ph-modal-user-info {",
        "  background: #0F1216; border: 1px solid #262D38; border-radius: 10px;",
        "  padding: 12px 14px; margin-bottom: 20px; display: flex; align-items: center; gap: 12px;",
        "}",
        ".ph-modal-avatar {",
        "  width: 34px; height: 34px; border-radius: 8px;",
        "  background: rgba(245, 158, 11, 0.15); color: #F59E0B;",
        "  display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 12px;",
        "}",
        ".ph-modal-actions { display: flex; align-items: center; justify-content: flex-end; gap: 10px; }",
        ".ph-btn-cancel {",
        "  padding: 9px 18px; background: transparent; border: 1px solid #334155;",
        "  border-radius: 8px; color: #CBD5E1; font-size: 13px; font-weight: 600;",
        "  cursor: pointer; transition: all 0.2s;",
        "}",
        ".ph-btn-cancel:hover { background: #1E293B; color: #FFFFFF; border-color: #475569; }",
        ".ph-btn-signout {",
        "  padding: 9px 20px; background: linear-gradient(135deg, #EF4444 0%, #DC2626 100%);",
        "  border: none; border-radius: 8px; color: #FFFFFF; font-size: 13px; font-weight: 600;",
        "  cursor: pointer; transition: all 0.2s; box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);",
        "  display: inline-flex; align-items: center; gap: 7px;",
        "}",
        ".ph-btn-signout:hover { background: linear-gradient(135deg, #DC2626 0%, #B91C1C 100%); transform: translateY(-1px); box-shadow: 0 6px 16px rgba(239, 68, 68, 0.4); }"
      ].join("\n");
      document.head.appendChild(style);
    }

    var modal = document.createElement("div");
    modal.id = "ph-logout-modal";
    modal.className = "ph-modal-backdrop";
    modal.setAttribute("role", "dialog");
    modal.setAttribute("aria-modal", "true");
    modal.setAttribute("aria-labelledby", "ph-logout-title");

    var userName = (session && session.name) ? session.name : "Current User";
    var userTitle = (session && session.title) ? session.title : "Stakeholder";
    var userInitials = (session && session.initials) ? session.initials : "PH";
    var userBadgeColor = (session && session.badge && session.badge.color) ? session.badge.color : "#F59E0B";

    modal.innerHTML = [
      '<div class="ph-modal-card">',
      '  <div class="ph-modal-header">',
      '    <div class="ph-modal-icon-box"><i class="fa-solid fa-arrow-right-from-bracket"></i></div>',
      '    <div>',
      '      <h3 class="ph-modal-title" id="ph-logout-title">Sign Out of PartsHub?</h3>',
      '      <p class="ph-modal-subtitle">You will end your active session and be safely redirected to the login portal.</p>',
      '    </div>',
      '  </div>',
      '  <div class="ph-modal-user-info">',
      '    <div class="ph-modal-avatar" style="background:' + userBadgeColor + '22; color:' + userBadgeColor + ';">' + userInitials + '</div>',
      '    <div style="flex:1; min-width:0;">',
      '      <div style="font-size:13px; font-weight:600; color:#FFFFFF; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">' + userName + '</div>',
      '      <div style="font-size:11px; color:#94A3B8;">' + userTitle + '</div>',
      '    </div>',
      '  </div>',
      '  <div class="ph-modal-actions">',
      '    <button type="button" class="ph-btn-cancel" id="ph-logout-cancel">Cancel</button>',
      '    <button type="button" class="ph-btn-signout" id="ph-logout-confirm"><i class="fa-solid fa-arrow-right-from-bracket"></i> Sign Out</button>',
      '  </div>',
      '</div>'
    ].join("");

    document.body.appendChild(modal);

    var cancelBtn = document.getElementById("ph-logout-cancel");
    if (cancelBtn) cancelBtn.addEventListener("click", PH.closeLogoutModal);

    var confirmBtn = document.getElementById("ph-logout-confirm");
    if (confirmBtn) {
      confirmBtn.addEventListener("click", function () {
        PH.logout();
      });
    }

    modal.addEventListener("click", function (e) {
      if (e.target === modal) PH.closeLogoutModal();
    });

    document.addEventListener("keydown", function (e) {
      if (e.key === "Escape" && modal.classList.contains("ph-modal-active")) {
        PH.closeLogoutModal();
      }
    });
  };

  /* ─────────────────────────────────────────────────────────────
   * 7. AUTOMATIC FETCH INTERCEPTOR (Attaches X-User-Role Header)
   * ───────────────────────────────────────────────────────────── */
  PH.setupFetchInterceptor = function () {
    if (window._phFetchInterceptorActive) return;
    window._phFetchInterceptorActive = true;

    var originalFetch = window.fetch;
    window.fetch = function (url, options) {
      options = options || {};
      options.headers = options.headers || {};

      var session = PH.getSession();
      if (session && session.role) {
        if (typeof Headers !== "undefined" && options.headers instanceof Headers) {
          if (!options.headers.has("X-User-Role")) {
            options.headers.append("X-User-Role", session.role);
          }
        } else if (typeof options.headers === "object") {
          if (!options.headers["X-User-Role"]) {
            options.headers["X-User-Role"] = session.role;
          }
        }
      }
      return originalFetch.call(this, url, options);
    };
  };

  /* ─────────────────────────────────────────────────────────────
   * 8. ROLE-AWARE NOTIFICATION BELL SYSTEM
   * ───────────────────────────────────────────────────────────── */
  PH.setupNotificationBell = function (session) {
    var bellBtn = document.querySelector(".icon-button[title='Alerts'], .icon-button[title='Notifications'], #btnNotificationBell");
    if (!bellBtn) return;

    var badgeCounter = bellBtn.querySelector(".badge-counter");
    
    var panelId = "ph-notification-panel";
    var panel = document.getElementById(panelId);
    if (!panel) {
      panel = document.createElement("div");
      panel.id = panelId;
      panel.style.cssText = "position: absolute; top: 55px; right: 80px; width: 340px; max-height: 420px; background: #1E293B; border: 1px solid #334155; border-radius: 10px; box-shadow: 0 10px 30px rgba(0,0,0,0.6); z-index: 2500; display: none; flex-direction: column; overflow: hidden; font-family: inherit;";
      document.body.appendChild(panel);
    }

    function fetchNotifications() {
      fetch('/api/notifications')
        .then(function(res) { return res.json(); })
        .then(function(data) {
          var unread = data.unreadCount || 0;
          if (badgeCounter) {
            badgeCounter.textContent = unread;
            badgeCounter.style.display = unread > 0 ? "inline-block" : "none";
          }
          renderPanelContent(data.notifications || []);
        })
        .catch(function(err) { console.log("Failed to load notifications:", err); });
    }

    function renderPanelContent(list) {
      var headerHtml = '<div style="padding: 12px 16px; border-bottom: 1px solid #334155; display: flex; justify-content: space-between; align-items: center; background: #0F172A;"><strong style="font-size: 13px; color: #F8FAFC;">Notifications</strong><button type="button" id="btnMarkAllRead" style="background: none; border: none; color: #38BDF8; font-size: 11px; cursor: pointer; font-weight: 600;">Mark all as read</button></div>';
      var bodyHtml = '<div style="overflow-y: auto; max-height: 340px; display: flex; flex-direction: column;">';
      if (list.length === 0) {
        bodyHtml += '<div style="padding: 24px; text-align: center; color: #94A3B8; font-size: 12px;">No notifications for your role.</div>';
      } else {
        list.forEach(function (n) {
          var unreadBg = n.read ? "transparent" : "rgba(56, 189, 248, 0.08)";
          var typeColor = n.type === 'DANGER' ? '#EF4444' : (n.type === 'WARNING' ? '#F59E0B' : (n.type === 'SUCCESS' ? '#10B981' : '#38BDF8'));
          bodyHtml += '<div class="ph-notif-item" data-id="' + n.id + '" style="padding: 10px 14px; border-bottom: 1px solid #334155; background: ' + unreadBg + '; cursor: pointer; transition: background 0.2s;">' +
            '<div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 4px;">' +
              '<span style="font-size: 12px; font-weight: 700; color: ' + typeColor + ';">' + escapeHtml(n.title) + '</span>' +
              (!n.read ? '<span style="width: 6px; height: 6px; border-radius: 50%; background: #38BDF8;"></span>' : '') +
            '</div>' +
            '<div style="font-size: 11.5px; color: #94A3B8; line-height: 1.4;">' + escapeHtml(n.message) + '</div>' +
          '</div>';
        });
      }
      bodyHtml += '</div>';
      panel.innerHTML = headerHtml + bodyHtml;

      var markAll = document.getElementById("btnMarkAllRead");
      if (markAll) {
        markAll.addEventListener("click", function () {
          fetch('/api/notifications/read-all', { method: 'POST' })
            .then(function () { fetchNotifications(); });
        });
      }

      panel.querySelectorAll(".ph-notif-item").forEach(function (item) {
        item.addEventListener("click", function () {
          var id = item.dataset.id;
          fetch('/api/notifications/' + id + '/read', { method: 'POST' })
            .then(function () { fetchNotifications(); });
        });
      });
    }

    bellBtn.addEventListener("click", function (e) {
      e.stopPropagation();
      var isVisible = panel.style.display === "flex";
      panel.style.display = isVisible ? "none" : "flex";
      if (!isVisible) fetchNotifications();
    });

    document.addEventListener("click", function (e) {
      if (panel && !panel.contains(e.target) && !bellBtn.contains(e.target)) {
        panel.style.display = "none";
      }
    });

    fetchNotifications();
  };

  /* ─────────────────────────────────────────────────────────────
   * 9. SELF-SERVICE ACCOUNT SETTINGS MODAL
   * ───────────────────────────────────────────────────────────── */
  PH.setupAccountSettingsModal = function (session) {
    var profileWidget = document.querySelector(".user-profile-widget, #topbar-user-name");
    if (!profileWidget) return;

    var modalId = "ph-account-modal";
    var modal = document.getElementById(modalId);
    if (!modal) {
      modal = document.createElement("div");
      modal.id = modalId;
      modal.style.cssText = "position: fixed; inset: 0; background: rgba(0,0,0,0.8); backdrop-filter: blur(8px); z-index: 3000; display: none; align-items: center; justify-content: center; padding: 20px;";
      modal.innerHTML = [
        '<div style="background: #1E293B; border: 1px solid #334155; border-radius: 12px; width: 100%; max-width: 440px; box-shadow: 0 20px 40px rgba(0,0,0,0.8); border-top: 3px solid #38BDF8; font-family: inherit; overflow: hidden;">',
        '  <div style="padding: 18px 22px; border-bottom: 1px solid #334155; display: flex; justify-content: space-between; align-items: center;">',
        '    <div><h3 style="margin: 0; font-size: 16px; color: #F8FAFC;">Account Credentials Settings</h3><div style="font-size: 11px; color: #94A3B8;">Self-Service Username & Password Manager</div></div>',
        '    <button type="button" id="btnCloseAccountModal" style="background: none; border: none; color: #94A3B8; font-size: 18px; cursor: pointer;">&times;</button>',
        '  </div>',
        '  <form id="phAccountSettingsForm" style="padding: 22px; display: flex; flex-direction: column; gap: 14px;">',
        '    <div style="font-size: 12px; color: #38BDF8; background: rgba(56,189,248,0.1); padding: 8px 12px; border-radius: 6px;">Logged in as: <strong id="accModalCurName">--</strong> (<span id="accModalRole">--</span>)</div>',
        '    <div style="display: flex; flex-direction: column; gap: 5px;">',
        '      <label style="font-size: 12px; color: #94A3B8;">Current Username</label>',
        '      <input type="text" id="accCurrentUsername" style="background: #0F172A; border: 1px solid #334155; border-radius: 6px; padding: 8px 12px; color: #FFF; font-size: 13px;" readonly />',
        '    </div>',
        '    <div style="display: flex; flex-direction: column; gap: 5px;">',
        '      <label style="font-size: 12px; color: #94A3B8;">Current Password *</label>',
        '      <input type="password" id="accCurrentPassword" placeholder="Required to verify identity" style="background: #0F172A; border: 1px solid #334155; border-radius: 6px; padding: 8px 12px; color: #FFF; font-size: 13px;" required />',
        '    </div>',
        '    <div style="display: flex; flex-direction: column; gap: 5px;">',
        '      <label style="font-size: 12px; color: #94A3B8;">New Username (Optional)</label>',
        '      <input type="text" id="accNewUsername" placeholder="Leave blank to keep current" style="background: #0F172A; border: 1px solid #334155; border-radius: 6px; padding: 8px 12px; color: #FFF; font-size: 13px;" />',
        '    </div>',
        '    <div style="display: flex; flex-direction: column; gap: 5px;">',
        '      <label style="font-size: 12px; color: #94A3B8;">New Password (Optional)</label>',
        '      <input type="password" id="accNewPassword" placeholder="Min 6 characters" style="background: #0F172A; border: 1px solid #334155; border-radius: 6px; padding: 8px 12px; color: #FFF; font-size: 13px;" />',
        '    </div>',
        '    <div id="accStatusMsg" style="font-size: 12px; display: none;"></div>',
        '    <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 10px;">',
        '      <button type="button" id="btnCancelAccountModal" style="background: transparent; border: 1px solid #334155; color: #94A3B8; padding: 8px 16px; border-radius: 6px; font-size: 13px; cursor: pointer;">Cancel</button>',
        '      <button type="submit" style="background: #38BDF8; border: none; color: #0F172A; font-weight: 700; padding: 8px 18px; border-radius: 6px; font-size: 13px; cursor: pointer;">Save Changes</button>',
        '    </div>',
        '  </form>',
        '</div>'
      ].join("");
      document.body.appendChild(modal);
    }

    profileWidget.style.cursor = "pointer";
    profileWidget.title = "Click to manage account settings & credentials";
    profileWidget.addEventListener("click", function () {
      var s = PH.getSession();
      document.getElementById("accModalCurName").textContent = s ? s.name : "User";
      document.getElementById("accModalRole").textContent = s ? s.title : "Role";
      document.getElementById("accCurrentUsername").value = s ? (s.username || s.name.toLowerCase().split(' ')[0]) : "";
      document.getElementById("accCurrentPassword").value = "";
      document.getElementById("accNewUsername").value = "";
      document.getElementById("accNewPassword").value = "";
      document.getElementById("accStatusMsg").style.display = "none";
      modal.style.display = "flex";
    });

    document.getElementById("btnCloseAccountModal").addEventListener("click", function () { modal.style.display = "none"; });
    document.getElementById("btnCancelAccountModal").addEventListener("click", function () { modal.style.display = "none"; });

    document.getElementById("phAccountSettingsForm").addEventListener("submit", function (e) {
      e.preventDefault();
      var s = PH.getSession();
      var curUser = document.getElementById("accCurrentUsername").value.trim();
      var curPass = document.getElementById("accCurrentPassword").value;
      var newUsername = document.getElementById("accNewUsername").value.trim();
      var newPassword = document.getElementById("accNewPassword").value;

      var msgEl = document.getElementById("accStatusMsg");

      fetch('/api/account/settings', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          currentUsername: curUser,
          currentPassword: curPass,
          newUsername: newUsername,
          newPassword: newPassword
        })
      })
      .then(function(res) { return res.json().then(function(data) { return { ok: res.ok, data: data }; }); })
      .then(function(result) {
        if (!result.ok) {
          msgEl.style.color = "#EF4444";
          msgEl.textContent = result.data.error || "Update failed.";
          msgEl.style.display = "block";
        } else {
          msgEl.style.color = "#10B981";
          msgEl.textContent = "Account updated successfully! Updating session...";
          msgEl.style.display = "block";
          setTimeout(function () {
            if (newUsername) session.username = newUsername;
            PH.saveSession(session);
            modal.style.display = "none";
          }, 1200);
        }
      })
      .catch(function(err) {
        msgEl.style.color = "#EF4444";
        msgEl.textContent = err.message;
        msgEl.style.display = "block";
      });
    });
  };

  /* ─────────────────────────────────────────────────────────────
   * 10. ROLE-BASED UI ACTION BUTTON HIDING
   * ───────────────────────────────────────────────────────────── */
  PH.enforceRoleUI = function (session, activeModuleKey) {
    if (!session || !session.role) return;
    var role = session.role;
    var isOwner = (role === "ROLE_OWNER" || role === "BUSINESS_OWNER");
    if (isOwner) return;

    var moduleOwnerMap = {
      inventory: ["ROLE_STOREKEEPER", "STOREKEEPER"],
      suppliers: ["ROLE_PROCUREMENT", "ROLE_PROCUREMENT_OFFICER", "PROCUREMENT_OFFICER", "PROCUREMENT"],
      purchases: ["ROLE_PURCHASING", "ROLE_PURCHASING_AGENT", "PURCHASING_AGENT", "PURCHASING"],
      sales:     ["ROLE_SALES", "ROLE_SALES_REP", "SALES_REP", "SALES"],
      returns:   ["ROLE_RETURNS", "ROLE_RETURNS_CLERK", "RETURNS_CLERK", "RETURNS"],
      dashboard: ["ROLE_OWNER", "BUSINESS_OWNER", "ROLE_BUSINESS_OWNER"]
    };

    var allowedRoles = moduleOwnerMap[activeModuleKey] || [];
    var isModuleOwner = allowedRoles.includes(role);

    if (!isModuleOwner) {
      var selectors = [
        "#btnOpenCreatePO", "#btnOpenAddModal", "#btnOpenCreateModal", "#btnSaveCampaign",
        ".btn-action-primary", ".btn-save-action", ".btn-new-campaign", ".btn-table-receive",
        ".btn-create-sale", ".btn-new-supplier", ".btn-add-line", ".btn-remove-row"
      ];
      selectors.forEach(function (sel) {
        document.querySelectorAll(sel).forEach(function (el) {
          if (!el.classList.contains("btn-secondary") && el.id !== "btn-logout" && el.id !== "btn-sidebar-logout") {
            el.style.display = "none";
          }
        });
      });
      document.querySelectorAll(".fa-trash-can, .fa-trash, .btn-delete").forEach(function (icon) {
        var btn = icon.closest("button") || icon.closest("a");
        if (btn) btn.style.display = "none";
      });
    }
  };

  function escapeHtml(str) {
    if (!str) return "";
    return String(str).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
  }

  /**
   * One-call init: runs module guard, renders nav & topbar, sets up logout system.
   * Place at the bottom of every protected page's <body>.
   * @param {string} moduleKey  — this page's module key
   */
  PH.initPage = function (moduleKey) {
    var session = PH.requireModule(moduleKey);
    if (!session) return;
    PH.setupFetchInterceptor();
    PH.renderSidebarNav(moduleKey, session.nav);
    PH.renderTopbar(session);
    PH.setupLogoutModal(session);
    PH.setupNotificationBell(session);
    PH.setupAccountSettingsModal(session);
    PH.enforceRoleUI(session, moduleKey);

    var logoutBtns = document.querySelectorAll("#btn-logout, #btn-sidebar-logout, .btn-logout, [data-action='logout']");
    for (var i = 0; i < logoutBtns.length; i++) {
      logoutBtns[i].addEventListener("click", function (e) {
        e.preventDefault();
        PH.openLogoutModal();
      });
    }
  };

})(window.PartsHub = window.PartsHub || {});
