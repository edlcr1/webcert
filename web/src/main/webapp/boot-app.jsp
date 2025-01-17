<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<%--
  ~ Copyright (C) 2022 Inera AB (http://www.inera.se)
  ~
  ~ This file is part of sklintyg (https://github.com/sklintyg).
  ~
  ~ sklintyg is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ sklintyg is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<!DOCTYPE html>
<html lang="sv" id="ng-app">
<head>

<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=Edge" />
<meta name="ROBOTS" content="nofollow, noindex" />
<meta name="viewport" content="width=device-width, initial-scale=1">

<title><spring:message code="application.name" /></title>

<!-- bower:css -->
<!-- endbower -->
<spring:message code="project.version" var="buildVersion" />
<c:set var="buildVersionNoDot" value="${fn:replace(buildVersion, '.', '')}" />
<c:set var="buildVersionNoDotOrDash" value="${fn:replace(buildVersionNoDot, '-', '')}" />

<link rel="stylesheet" href="/app/webcert.css?cacheSlayer=${buildVersionNoDotOrDash}"
      media="screen">
<link rel="stylesheet" href="/web/webjars/common/webcert/wc-common.css?cacheSlayer=${buildVersionNoDotOrDash}"
      media="screen">

</head>
<body>

  <wc-service-banner id="service-banners"></wc-service-banner>

  <div ui-view="header" autoscroll="true" id="wcHeader"></div>
  <div class="wc-main-menu-wrapper">
    <wc-main-menu></wc-main-menu>
  </div>

  <wc-missing-subscription-banner></wc-missing-subscription-banner>

  <wc-modal-user-survey app-label="'wc'"></wc-modal-user-survey>

<%-- ui-view that holds dynamic content managed by angular app --%>
  <div ui-view="content" autoscroll="false" id="view" class="webcert-workarea"></div>

  <div ui-view="landing" autoscroll="false" id="webcertLanding" class="webcert-landing"></div>

  <%-- No script to show at least something when javascript is off --%>
  <noscript>
    <h1>
      <span><spring:message code="error.noscript.title" /></span>
    </h1>
    <div class="alert alert-danger">
      <spring:message code="error.noscript.text" />
    </div>
  </noscript>

  <c:choose>
    <c:when test="${pageAttributes.useMinifiedJavaScript == 'true'}">

      <script type="text/javascript">
          var WEBCERT_DEBUG_MODE = false;
      </script>
      <script type="text/javascript" src="/app/vendor.min.js?cacheSlayer=${buildVersionNoDotOrDash}"></script>
      <script type="text/javascript" src="/app/app.min.js?cacheSlayer=${buildVersionNoDotOrDash}"></script>
    </c:when>
    <c:otherwise>
      <script type="text/javascript">
          var WEBCERT_DEBUG_MODE = true;
      </script>

      <!-- bower:js -->
      <script type="text/javascript" src="/bower_components/jquery/dist/jquery.js"></script>
      <script type="text/javascript" src="/bower_components/angular/angular.js"></script>
      <script type="text/javascript" src="/bower_components/angular-animate/angular-animate.js"></script>
      <script type="text/javascript" src="/bower_components/angular-cookies/angular-cookies.js"></script>
      <script type="text/javascript" src="/bower_components/angular-i18n/angular-locale_sv-se.js"></script>
      <script type="text/javascript" src="/bower_components/angular-sanitize/angular-sanitize.js"></script>
      <script type="text/javascript" src="/bower_components/angular-bootstrap/ui-bootstrap-tpls.js"></script>
      <script type="text/javascript" src="/bower_components/angular-ui-router/release/angular-ui-router.js"></script>
      <script type="text/javascript" src="/bower_components/bootstrap-sass/assets/javascripts/bootstrap.js"></script>
      <script type="text/javascript" src="/bower_components/momentjs/moment.js"></script>
      <script type="text/javascript" src="/bower_components/oclazyload/dist/ocLazyLoad.js"></script>
      <script type="text/javascript" src="/bower_components/highcharts/highcharts.js"></script>
      <script type="text/javascript" src="/bower_components/highcharts/modules/accessibility.js"></script>
      <!-- endbower -->
      <script type="text/javascript" src="/bower_components/angular-ui-router/release/stateEvents.js"></script>
      <script type="text/javascript" src="/vendor/polyfill.js"></script>
      <script type="text/javascript" src="/vendor/angular-smooth-scroll.js"></script>
      <script type="text/javascript" src="/vendor/angular-shims-placeholder/angular-shims-placeholder.js"></script>
      <script type="text/javascript" src="/vendor/_convert.js"></script>
      <script type="text/javascript" src="/vendor/_language.js"></script>
      <script type="text/javascript" src="/vendor/_utility.js"></script>
      <script type="text/javascript" src="/vendor/netid-1.1.5.js"></script>
      <script type="text/javascript" src="/app/app.js"></script>
    </c:otherwise>
  </c:choose>
</body>
</html>
