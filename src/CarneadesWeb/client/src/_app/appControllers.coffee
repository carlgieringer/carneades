# Copyright (c) 2014 Fraunhofer Gesellschaft
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

define ['angular', 'common/services/i18nNotifications', 'common/services/httpRequestTracker', 'common/resources/themes'], (angular) ->
  "use strict"
  angular.module('app.controllers', ['services.i18nNotifications', 'services.httpRequestTracker', 'resources.themes'])

  .controller('AppCtrl', ($scope, $stateParams, $location, i18nNotifications) ->
    ( ->
      carneades = (converter) ->
        [
          #  ? title ? syntax
          type: "lang"
          regex: "\\[@([^\\,]+)[^\\]]*\\]"
          replace: (match, citation_key) ->
            "<a href='" + "/carneades/#/projects/#{$stateParams.pid}/#{$stateParams.db}/outline?scrollTo=#{citation_key}" + "'>#{match}</a>";
        ,
          type: "output"
          filter: (source) ->
            source.replace /file:\/\/(\w+)\/(\w+)/g, (match, project, document) ->
              "carneadesws/documents/" + project + "/" + document
        ]

      # Client-side export
      window.Showdown.extensions.carneades = carneades  if typeof window isnt "undefined" and window.Showdown and window.Showdown.extensions

      # Server-side export
      module.exports = carneades  if typeof module isnt "undefined"

      undefined
    )()

    undefined
  )

  .controller 'HeaderCtrl', ($breadcrumb, $scope, $location, notifications, httpRequestTracker) ->
    $scope.hasPendingRequests = ->
      httpRequestTracker.hasPendingRequests()

    setNavigationState = () ->
      $scope.$navigationStates = $breadcrumb.getNavigationStates $scope.$state, $scope.$stateParams

    $scope.$on '$stateChangeSuccess', ->
      setNavigationState()

    undefined

  .directive('bcNavigation', () ->
    restrict: 'E'
    replace: 'true'
    template: '<div class=\"my-fluid-container\" ng-controller="HeaderCtrl"><breadcrumb states="$navigationStates" style="style"></breadcrumb></div>'
    controller: ($scope, $element, $attrs, $stateParams) ->
      if $stateParams.pid is 'markos'
        $scope.style = 'simple'
      else
        $scope.style = 'emacs'

      setTheme = () ->
        if $stateParams.pid is 'markos'
          $scope.style = 'simple'
        else
          $scope.style = 'emacs'

      $scope.$on '$stateChangeSuccess', ->
        setTheme()
    )

  .directive 'cssInject', ($compile, $stateParams) ->
    restrict: 'E'
    replace: true
    template: '<link rel="stylesheet" ng-href="api/projects/{{theme}}/theme/css/{{theme}}.css" media="screen"/>'
    scope:
      defaultTheme: '@'
    controller: ($scope, $element, $attrs, $stateParams) ->
      unless $scope.theme then $scope.theme = $scope.defaultTheme
      setTheme = () ->
        if $stateParams.pid and $scope.theme isnt $stateParams.pid
          $scope.theme = $stateParams.pid

      $scope.$on '$stateChangeSuccess', ->
        setTheme()

  .directive 'projectBanner', ($compile) ->
    restrict: 'E'
    replace: 'true'
    scope:
      display: '@'
    template: '<div ng-include="\'api/projects/\' + theme + \'/theme/html/banner.tpl\'"></div>'
    controller: ($scope, $element, $attrs, $stateParams) ->
      unless $scope.display then $scope.theme = 'default'
      setTheme = () ->
        if $stateParams.pid and $scope.default isnt $stateParams.pid
          $scope.theme = $stateParams.pid

      $scope.$on '$stateChangeSuccess', ->
        setTheme()

  .directive 'projectFooter', ($compile) ->
    restrict: 'E'
    replace: 'true'
    scope:
      display: '@'
    template: '<div ng-include="\'api/projects/\' + theme + \'/theme/html/footer.tpl\'"></div>'
    controller: ($scope, $element, $attrs, $stateParams) ->
      unless $scope.display then $scope.theme = 'default'
      setTheme = () ->
        if $stateParams.pid and $scope.default isnt $stateParams.pid
          $scope.theme = $stateParams.pid

      $scope.$on '$stateChangeSuccess', ->
        setTheme()
