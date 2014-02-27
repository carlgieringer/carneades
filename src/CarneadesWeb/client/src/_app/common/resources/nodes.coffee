# Copyright (c) 2014 Fraunhofer Gesellschaft
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

#global define
define ["angular", "angular-resource"], (angular) ->
  "use strict"
  services = angular.module("resources.nodes", ["ngResource"])
  services.factory "Node", ["$resource", ($resource) ->
    $resource "../api/projects/:pid/:db/nodes/:nid",
      pid: "@pid"
      db: "@db"
      nid: "@nid"

  ]
  services.factory "MultiNodeLoader", ["Node", "$q", (Node, $q) ->
    ->
      delay = $q.defer()
      Node.query ((nodes) ->
        delay.resolve nodes
      ), ->
        delay.reject "Unable to fetch nodes"

      delay.promise
  ]
  services.factory "NodeLoader", ["Node", "$q", (Node, $q) ->
    (params) ->
      delay = $q.defer()
      Node.get params, ((node) ->
        delay.resolve node
      ), ->
        delay.reject "Unable to fetch argument!"

      delay.promise
  ]
  services
