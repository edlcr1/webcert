/**
 * Common directives used in both MI as well as in modules. Since this js will be used/loaded from 
 * different contextpaths, all templates are inlined. PLEASE keep source formatting in this 
 * file as-is, otherwise the inline templates will be hard to follow. 
 */
angular.module('wc.common.directives', []);

angular.module('wc.common.directives').directive("wcHeader", ['$rootScope', function($rootScope) {
    return {
        restrict : "A",
        replace : true,
        scope : {
          userName: "@"
        },
        template:
        	'<div>'
        	+'<div class="row-fluid">'
        		+'<div class="span3">'
        			+'<div class="logoline"><a href="/web/start"><img alt="Till startsidan" src="/img/webcert_logo.png"/></a></div>'
        		+'</div>'
        		+'<div class="span3">'
        			+'2013-05-06'
        		+'</div>'
        		+'<div class="span3">'
        			+'<div class="span2"><img src="/img/avatar.png"/></div>'
        			+'<div class="span10" ng-show="userName.length">'
        				+'<strong>Läkare</strong> - <span class="logged-in">{{userName}}</span><br>'
        				+'<span class="location">ABC Landstinget Västmanland</span>'
        			+'</div>'
        		+'</div>'
	        	+'<div class="span3">'
		    			+'<div class="dropdown">'
		    				+'<a class="dropdown-toggle settings" data-toggle="dropdown" href="#"></a>'
		    				+'<ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">'
		    					+'<li><a tabindex="-1" href="#">Action</a></li>'
		    					+'<li class="divider"></li>'
		    					+'<li><a tabindex="-1" href="#">Separated link</a></li>'
		    				+'</ul>'
		    			+'</div>'				
	    			+'</div>'
    			+'</div>'
    		+'</div>'  
    }
} ]);
