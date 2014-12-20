define(['services/logger','durandal/app', 'viewmodels/account', 'viewmodels/changePasswordModal'], function (logger,app,Account,ChangePasswordModal) {
    var etag = 0;
    var cachedAccount;
    var userId = $("#userId").val();
    var AccountModal = function() {
        ko.validation.configure({
            insertMessages: false,
            decorateElement: true,
            errorElementClass: 'error'
        });

        // Data
        var self = this;

        self.email = ko.observable('').extend({ email: true });
        self.updatingEmail = ko.observable(false);

        self.accountName = ko.observable('').extend({minLength : 2});
        self.updatingName = ko.observable(false);
        self.updatingName(true);

        var ajaxParams = {
            contentType: "application/json",
            success: function (data, textStatus, jqXHR) {
                etag = jqXHR.getResponseHeader('ETag');
                var dataUsed;
                if (jqXHR.status === 304) {
                    dataUsed = cachedAccount;
                }
                else {
                    cachedAccount = data;
                    dataUsed = data;
                }
                var act = new Account(dataUsed);
                self.email(act.email);
                self.accountName(act.name);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                var json = JSON.parse(jqXHR.responseText);
                if (json.redirect) {
                    window.location = json.redirect;
                }
            }
        };
        if (etag !== 0) {
            ajaxParams.headers = { "If-None-Match": etag }
        }

        $.ajax("/api/account/" + userId, ajaxParams);

        self.activate = function() {
           logger.log('AccountModal activated', null, 'accountModal');
           return true;
        };


        self.updateName = function() {
            self.updatingName(true);
            $.ajax("/api/account/" + userId + "/name",
                {data: ko.toJSON(self.accountName)
                 , type: "put"
                 , contentType: "application/json"
                 , headers: { "sessionCsfrToken": $("#sessionCsfrToken").val() }
                 , statusCode: {
                    202: function(data,textStatus,jqXHR) { /*Do something useful*/ },
                    400: function(jqXHR,textStatus,errorThrown) { /*Do something useful  */ },
                    401: function(jqXHR,textStatus,errorThrown) { /*Re-authenticate user */ },
                    500: function(jqXHR,textStatus,errorThrown) { /*Do something useful  */ },
                    503: function(jqXHR,textStatus,errorThrown) { /*Do something useful  */ }
                    }
                 }
            ).always(function() { self.updatingName(false);  });
        };


        self.updateEmail = function() {
            self.updatingEmail(true);
            $.ajax("/api/account/" + userId +  "/email",
                {data: ko.toJSON(self.email)
                 , type: "put"
                 , contentType: "application/json"
                 , headers: { "sessionCsfrToken": $("#sessionCsfrToken").val() }
                 , statusCode: {
                    202: function(data,textStatus,jqXHR) { /*Do something useful*/ },
                    400: function(jqXHR,textStatus,errorThrown) { /*Do something useful  */ },
                    401: function(jqXHR,textStatus,errorThrown) { /*Re-authenticate user */ },
                    500: function(jqXHR,textStatus,errorThrown) { /*Do something useful  */ },
                    503: function(jqXHR,textStatus,errorThrown) { /*Do something useful  */ }
                    }
                 }
            ).always(function() { self.updatingEmail(false);  });
        };


        self.showPasswordDialog = function() {
            app.showModal(new ChangePasswordModal());
        };

        self.ok = function() {
            self.modal.close();
        }
    };
    return  (AccountModal);
});