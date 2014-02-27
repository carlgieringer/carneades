# Copyright (c) 2014 Fraunhofer Gesellschaft
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

define ['angular', './licanResources', './licanStates', './licanControllers', 'templates/lican'], (angular) ->
  "use strict"
  angular.module('lican.module', ['lican.controllers', 'lican.states', 'lican.resources', 'ngResource', 'templates.lican'])
  .constant 'messages',
    intro_text: 'Click start to begin an in-depth analysis of the licensing issues. You will be ask questions if futher input is needed. You will then be shown an outline of the initial analysis. You can click on the map button to see a diagrammatic view of the analysis.'
    intro_start: 'Start!'
    intro_title: 'License Analysis of '
    send: 'Send'
    questions_title: 'Questions'
