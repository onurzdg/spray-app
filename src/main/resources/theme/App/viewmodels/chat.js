define(['services/logger','durandal/app','viewmodels/chatmessage'], function (logger,app,ChatMessage) {

    function vm() {
        var self = this;
        self.title = 'Chat';

        self.messages = ko.observableArray([]);
        self.newChatMessage = ko.observable('');

        self.sendChatMessage = function() {
            $.ajax("/api/chat",
                {data: ko.toJSON({"message": self.newChatMessage})
                    , type: "post"
                    , contentType: "application/json"
                    , headers: { "sessionCsfrToken": $("#sessionCsfrToken").val() }
                    , error: function(jqXHR, textStatus, errorThrown) {
                        var json = JSON.parse(jqXHR.responseText);
                        if(json.redirect) {
                            window.location = json.redirect;
                        }

                    }
                }
             ).always(function() { self.newChatMessage(''); })
        };

        // event source
        self.makeEventSource = function() {
           var s = new EventSource("/streaming/chat");
           s.addEventListener("message", function(e) {
           var parsed = JSON.parse(e.data);
           var msg = new ChatMessage(parsed);

            self.messages.push(msg);
           }, false);
           return s;
        };

        self.source = self.makeEventSource();



        // Durandal vm lifecycle
        self.activate = function() {
            logger.log('Chat activated', null, 'chat');
            return true;
        };

        self.deactivate = function() {
            self.source.close();
        };


    }

    return (new vm());



});