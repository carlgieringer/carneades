# Copyright (c) 2014 Fraunhofer Gesellschaft
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

define [
  'angular',
  'angular-translate'
], (angular) ->
  angular.module('ag.controllers', [
    'pascalprecht.translate'
  ])

  .controller('CreateAgCtrl', ($scope, $state, $stateParams, ag) ->
    
    $scope.ag =
      name: "",
      header:
        description: {en: "", de: "", fr: "", it: "", sp: "", nl: ""},
        title: ""

    $scope.onSave = ->
      ag.save($stateParams, $scope.ag).$promise.then(
        (v) ->
          $state.transitionTo 'home.projects.project.outline', {pid: $stateParams.pid, db: $scope.ag.name}
        (e) ->
          console.log 'error', e
      )  
  )