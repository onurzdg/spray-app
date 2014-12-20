define(['durandal/system','durandal/app','durandal/plugins/router', 'services/logger', 'viewmodels/accountModal'],
    function (system,app,router,logger,AccountModal) {


        var adminRoutes = ko.computed(function () {
            return router.allRoutes().filter(function (r) {
                return r.settings.admin;
            });
        });

        var shell = {
            activate: activate,
            adminRoutes: adminRoutes,
            router: router,
            showAccountModal: showAccountModal,
            signOut: signOut
        };
        
        return shell;

        function activate() {
            return boot();
        }

        function boot() {
            router.mapNav('chat');
            router.mapNav('view2');
            log('App loaded', null);
            return router.activate('chat');
        }


        function signOut() {

            $.ajax({
                type: "POST",
                url: '/api/signout',
                headers: { "sessionCsfrToken": $("#sessionCsfrToken").val() },
                success: function(json) {
                    json = json || {};
                    if (json.success) {
                        window.location = json.redirect || location.href;
                    } else if (json.errors) {
                        //displayErrors($form, json.errors);
                    }
                },
                error: function(jqXHR) {
                    if(jqXHR.status === 401) {
                        window.location = "/"
                    }
                }
            });
        }

        function showAccountModal() {
            app.showModal(new AccountModal());
        }


        function log(msg, data) {
            logger.log(msg, data, system.getModuleId(shell));
        }

    });