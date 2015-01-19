describe('ViewCertCtrl', function() {
    'use strict';

    var manageCertificateSpy;
    var $routeParams;
    var $httpBackend;
    var dialogService;
    var $scope;
    var $qaCtrlScope;
    var $q;
    var $rootScope;
    var $location;
    var $window;
    var modalMock;
    var newUrl = 'http://server/#/app/new';
    var currentUrl = '/app/new';
    var UserPreferencesService;
    var $controller;

    function MockDeferreds($q){
        this.$q = $q;
        this.defs = [];
        this.lastPopped = null;
        this.getDeferred = function(){
            var def = this.$q.defer();
            this.defs.push(def);
            return def;
        };
        this.popDeferred = function(){
            this.lastPopped = this.defs.pop();
            return this.lastPopped;
        };
        this.getLast = function(){
            var index = 0;
            if(this.defs.length > 0 ){
                index = this.defs.length - 1;
            }
            var def = this.defs[index];
            return def;
        };
        this.resolveLast = function(value){
            this.getLast().resolve(value);
        };
        this.getLastPopped = function(){
            return this.lastPopped;
        }
    };

    var mockDeferreds;

    // Load the webcert module and mock away everything that is not necessary.
    beforeEach(angular.mock.module('webcert', function($provide) {
        dialogService = jasmine.createSpyObj('common.dialogService', [ 'showDialog' ]);
        modalMock = jasmine.createSpyObj('modal', [ 'close' ]);
        dialogService.showDialog.andCallFake(function(){
            return modalMock;
        });
        $provide.value('common.dialogService', dialogService);
        $provide.value('$window', {location:{href:currentUrl}});
        manageCertificateSpy = jasmine.createSpyObj('webcert.ManageCertificate', [ 'getCertType' ]);
        $provide.value('webcert.ManageCertificate', manageCertificateSpy);

        UserPreferencesService = jasmine.createSpyObj('UserPreferencesService', [ 'isSkipShowUnhandledDialogSet' ]);
        $provide.value('common.UserPreferencesService', UserPreferencesService);

        $routeParams = {qaOnly:false};
        $provide.value('$routeParams', $routeParams);


    }));

    // Get references to the object we want to test from the context.
    beforeEach(angular.mock.inject([ '$controller', '$rootScope', '$q', '$httpBackend', '$location', '$window',
        function( _$controller_, _$rootScope_,_$q_,_$httpBackend_, _$location_, _$window_) {

            $rootScope = _$rootScope_;
            $scope = $rootScope.$new();
            $qaCtrlScope = $rootScope.$new();
            $q = _$q_;
            $httpBackend = _$httpBackend_;
            $location = _$location_;
            $window = _$window_;
            mockDeferreds = new MockDeferreds($q);

            spyOn($scope, '$broadcast');

            // setup the current location
            $location.url(currentUrl);

            $routeParams.qaOnly = false;

            $controller = _$controller_;
            $controller('webcert.ViewCertCtrl',
                { $rootScope: $rootScope, $scope: $scope });

        }])
    );


    describe('#checkSpecialQALink', function() {
        it('Check if the user used the special qa-link to get here', function(){


            $routeParams.qaOnly = true;

            $controller('webcert.ViewCertCtrl',
                { $rootScope: $rootScope, $scope: $scope });

            // ----- arrange
            expect(manageCertificateSpy.getCertType).toHaveBeenCalled();

            // kick off the window change event
            $rootScope.$broadcast('$locationChangeStart', newUrl, currentUrl);

            // ------ act
            // promises are resolved/dispatched only on next $digest cycle
            $rootScope.$apply();

            // ------ assert
            // dialog should be opened
            expect(dialogService.showDialog).toHaveBeenCalled();

        });

    });

    describe('#checkHasNoUnhandledMessages', function() {
        it('should check that a dialog is not opened, if there are no unhandled messages, and go to then newUrl', function(){

            // ----- arrange
            expect(manageCertificateSpy.getCertType).toHaveBeenCalled();

            // spy on the defferd
            var def = mockDeferreds.getDeferred();
            spyOn($q, 'defer').andReturn(def);

            // kick off the window change event
            //$rootScope.$broadcast('$locationChangeStart', newUrl, currentUrl);

            var areThereUnhandledMessages = false;
            mockDeferreds.getLast().resolve(areThereUnhandledMessages);

            // ------ act
            // promises are resolved/dispatched only on next $digest cycle
            $rootScope.$apply();

            // ------ assert
            expect($scope.$broadcast).toHaveBeenCalledWith('hasUnhandledQasEvent', mockDeferreds.popDeferred());

        });

    });

    describe('#checkUserPreferencesService', function() {
        it('should check that cookie service isSkipShowUnhandledDialog is true', function(){
            // ----- arrange
            // spy on the defferd
            var def = mockDeferreds.getDeferred($q);
            spyOn($q, 'defer').andReturn(def);
            UserPreferencesService.isSkipShowUnhandledDialogSet.andReturn(true);

            // ------ act
            // kick off the window change event

            // promises are resolved/dispatched only on next $digest cycle
            $rootScope.$apply();

            $rootScope.$broadcast('$locationChangeStart', newUrl, currentUrl);

            mockDeferreds.getLast().resolve(false);


            // ------ assert
            expect(manageCertificateSpy.getCertType).toHaveBeenCalled();

            expect($scope.$broadcast).not.toHaveBeenCalledWith('hasUnhandledQasEvent', mockDeferreds.popDeferred());

        });

    });

    describe('#checkHasUnhandledMessages', function() {

        beforeEach(function(){
            console.debug("---- before each");
            // the below is run before each sub test as a means to fire a location change event and so opening the dialog.
            // ----- arrange
            // setup 3 deferreds, for some weird reason we have to do this
            mockDeferreds.getDeferred();
            mockDeferreds.getDeferred();
            mockDeferreds.getDeferred();

            spyOn($q, 'defer').andCallFake(function() {
                return mockDeferreds.popDeferred();
            });

            UserPreferencesService.isSkipShowUnhandledDialogSet.andReturn(false);

            // ------ act
            console.debug("+++ before apply");

            mockDeferreds.getLast().resolve(true);

            // promises are resolved/dispatched only on next $digest cycle
            $rootScope.$apply();

            console.debug("-- after apply");
            console.debug("before location change");

            $rootScope.$broadcast('$locationChangeStart', newUrl, currentUrl);


            console.debug("after location change");
            // ------ assert
            expect(manageCertificateSpy.getCertType).toHaveBeenCalled();
            expect(UserPreferencesService.isSkipShowUnhandledDialogSet).toHaveBeenCalled();
            expect($scope.$broadcast).toHaveBeenCalledWith('hasUnhandledQasEvent', mockDeferreds.getLastPopped());

            // dialog should be opened
            expect(dialogService.showDialog).toHaveBeenCalled();

        });

        it('just check that the dialog is opened and the url is the same', function(){
            // the url wont be changed until a button is pressed!!
            expect($window.location.href).toEqual(currentUrl);
        });

        describe('#buttonHandle', function() {
            it('handle button click', function(){
                console.debug("+++ buttonHandle");
                // inside the handled button click, test that :
                var args = dialogService.showDialog.mostRecentCall.args;
                var dialogOptions = args[1];
                // press the handled button
                dialogOptions.button1click();


                mockDeferreds.getLastPopped().resolve(true);
                //resolve the deffereds, because the secound  is within deferred 1 we need to call apply on root, again to resolve further deferreds ..
                $rootScope.$apply();

                // markAllAsHandledEvent is broadcast
                expect($scope.$broadcast).toHaveBeenCalledWith('markAllAsHandledEvent', mockDeferreds.getLastPopped());

                // modal is closed
                expect(modalMock.close).toHaveBeenCalled();
                // the url wont be changed until a button is pressed!!
                expect($window.location.href).toEqual(newUrl);
                console.debug("--- buttonHandle");
            });
        });

        describe('#buttonUnHandle', function() {
            it('un handled button click', function(){
                // inside the handled button click, test that :
                var args = dialogService.showDialog.mostRecentCall.args;
                var dialogOptions = args[1];
                // press the not handled button
                dialogOptions.button2click();
                // no action is taken, just close the dialog
                // modal is closed
                expect(modalMock.close).toHaveBeenCalled();
                // the url wont be changed until a button is pressed!!
                expect($window.location.href).toEqual(newUrl);
            });
        });

        describe('#buttonBack', function() {
            it('back button click', function(){
                // inside the handled button click, test that :
                var args = dialogService.showDialog.mostRecentCall.args;
                var dialogOptions = args[1];
                // press the back button
                dialogOptions.button3click();
                // no action is taken, just close the dialog
                // modal is closed
                expect(modalMock.close).toHaveBeenCalled();
                // the url should be the same no changes
                expect($window.location.href).toEqual(currentUrl);
            });
        });


    });

});