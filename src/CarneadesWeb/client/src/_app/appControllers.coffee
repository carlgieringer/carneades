# Copyright (c) 2014 Fraunhofer Gesellschaft
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

define ['angular', 'common/services/i18nNotifications', 'common/services/httpRequestTracker'], (angular) ->
  "use strict"
  angular.module('app.controllers', ['services.i18nNotifications', 'services.httpRequestTracker'])
  .directive('bcNavigation', () ->
    restrict: 'E'
    replace: 'true'
    template: '<div><header class="navbar-inverse navbar-fixed-top" ng-controller="HeaderCtrl"><breadcrumb states="$navigationStates"></breadcrumb></header></div>'
  )
  .controller('AppCtrl', ($scope, $location, i18nNotifications) ->
    $scope.notifications = i18nNotifications
    $scope.removeNotification = (notification) ->
      i18nNotifications.remove(notification)
      undefined

    $scope.$on '$routeChangeError', (event, current, previous, rejection) ->
       i18nNotifications.pushForCurrentRoute 'errors.route.changeError', 'error', {}, {rejection: rejection}
       undefined

     ( ->
        "use strict"
        carneades = (converter) ->
          [
            #  ? title ? syntax
            type: "lang"
            regex: "\\[@([a-zA-Z0-9]+)[^\\]]*\\]"
            replace: (match, citation_key) ->
              metadata = {}

              request = new XMLHttpRequest
              request.open 'GET', $location.protocol() + "://" + $location.host() + ":" + $location.port() + "/carneades/api/projects/copyright/main/metadata?k=#{citation_key}", false

              request.onload = ->
                if request.status >= 200 and request.status < 400
                  metadata = JSON.parse request.responseText
                else
                  # We reached our target server, but it returned an error
                  console.log 'Server reached, but it returned an error'

                request.onerror = ->
                  # There was a connection error of some sort
                  console.log 'Error'

              request.send()

              metadatum = undefined
              if metadata and metadata.length is 1
                metadatum = metadata[0].source
                return "<a href=\"" + metadatum + "\" >" + match + "</a>" if metadatum
                metadatum = metadata[0].identifier
                return "<a href=\"" + metadatum + "\" >" + match + "</a>"  if metadatum and (metadatum.indexOf("http://") is 0 or metadatum.indexOf("https://") is 0 or metadatum.indexOf("ftp://") is 0 or metadatum.indexOf("file://") is 0)
              match
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
