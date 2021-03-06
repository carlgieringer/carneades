# Copyright (c) 2014 Fraunhofer Gesellschaft
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
define [
  "angular",
  "angular-ui-router"
], (angular) ->
  angular.module("directives.pagenav", [
    "ui.router.state"
  ])

  .directive 'pageNavigation', () ->
    restrict: 'E'
    replace: true
    transclude: true
    templateUrl: 'common/directives/page-navigation/page-navigation.jade'

  .directive 'pageNavigationFull', () ->
    restrict: 'E'
    replace: true
    transclude: true
    templateUrl: 'common/directives/page-navigation/page-navigation-full.jade'

  .directive 'pageNavigationSmOffset2', () ->
    restrict: 'E'
    replace: true
    transclude: true
    templateUrl: 'common/directives/page-navigation/page-navigation-sm-offset-2.jade'

  .directive 'pageNavigationItem', () ->
    restrict: 'E'
    replace: true
    templateUrl: 'common/directives/page-navigation/page-navigation-item.jade'
    scope:
      cmd: '='
    controller: ($scope, $element, $attrs, $state, $stateParams) ->
      $scope.navOpen = (name) ->
        $state.go name, $stateParams
