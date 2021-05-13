/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

angular.module('webcert').controller('webcert.SubscriptionCtrl', ['$log', '$rootScope', '$scope', '$window','$sanitize', '$state',
    '$location', 'common.UserModel', 'common.subscriptionService', 'common.dynamicLinkService',
    function($log, $rootScope, $scope, $window, $sanitize, $state, $location, UserModel, subscriptionService, dynamicLinkService) {
    'use strict';

    UserModel.transitioning = false;

    $scope.modalBody = {
        bodyTextId: 'subscription-modal-body-text',
        info: 'subscription.during.adjustment.info.text',
        eleg: subscriptionService.isElegUser() ? 'subscription.during.adjustment.eleg.text' : '',
        links: 'subscription.during.adjustment.link.text'
    };

    $scope.modalOptions = {
        body: $scope.modalBody,
        modalBodyTemplateUrl: '/app/views/subscription/subscription.body.html',
        titleId: 'subscription.during.adjustment.title.text',
        buttons: [
            {
                name: 'subscription.sign.agreement.now',
                clickFn: function() {
                    subscriptionService.acknowledgeSubscriptionInfoForCareUnit();
                    $scope.modalOptions.modalInstance.dismiss('cancel');
                    $window.open(dynamicLinkService.getLink('kundportalenGetAccount').url);
                    $state.transitionTo('webcert.create-index');
                },
                text: 'subscription.sign.agreement.now.label',
                id: 'subscriptionSignAgreementNowBtn',
                className: 'btn-primary'
            },
            {
                name: 'subscription.sign.agreement.later',
                clickFn: function() {
                    subscriptionService.acknowledgeSubscriptionInfoForCareUnit();
                    $scope.modalOptions.modalInstance.dismiss('cancel');
                    $state.transitionTo('webcert.create-index');
                },
                text: 'subscription.sign.agreement.later.label',
                id: 'subscriptionSignAgreementLaterBtn',
                className: 'btn-default'
            }
      ],
      showClose: false
    };
  }]
);
