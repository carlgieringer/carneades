# Copyright (c) 2014 Fraunhofer Gesellschaft
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

define ["angular", "angular-resource"], (angular) ->
  "use strict"
  services = angular.module("resources.statements", ["ngResource"])
  services.factory "Statement", ["$resource", ($resource) ->
    $resource "../api/projects/:pid/:db/statements/:sid",
      pid: "@pid"
      db: "@db"
      sid: "@sid"

  ]
  services.factory "MultiStatementLoader", ["Statement", "$q", (Statement, $q) ->
    ->
      delay = $q.defer()
      Statement.query ((statement) ->
        delay.resolve statement
      ), ->
        delay.reject "Unable to fetch nodes"

      delay.promise
  ]
  services.factory "StatementLoader", ["Statement", "$q", (Statement, $q) ->
    (params) ->
      delay = $q.defer()
      Statement.get params, ((statement) ->
        delay.resolve statement
      ), ->
        delay.reject "Unable to fetch argument!"

      delay.promise
  ]
  services