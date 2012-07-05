eXo = eXo || {};

eXo.OpenAjax = {
	init : function(args) {
		this.onSubscribe = args.onSubscribe;
		this.onUnsubscribe = args.onUnsubscribe;
		this.onPublish = args.onPublish;
		this.scope = args.scope | window;
		this.log = args.log | console.log;

		this.hub = new OpenAjax.hub.ManagedHub({
	      	onPublish: this.onPublish,
		onSubscribe: this.onSubscribe,
		onUnsubscribe: this.onUnsubscribe,
		scope: this.scope,
		log: this.log
		});
	}
};

eXo.OpenAjax.init({
	onSubscribe: function(topic, container) {
		var output = document.getElementById("subscribe");
		output.innerHTML = "abc subscribes to topic '" + topic + "'";
		return true;
		// return false to reject the request.
	},
	onUnsubscribe: function(topic, container) {
		var output = document.getElementById("subscribe");
		output.innerHTML = "abc unsubscribes from topic '" + topic + "'";
	},
	onPublish: function(topic, data, pcont, scont) {
		var output = document.getElementById("publish");
		output.innerHTML = "abc publishes '" + data + "' to topic '" + topic + "' subscribed by " + "abc";
		return true;
		// return false to reject the request.
	},
	scope: window,
	log: console.log
});
	
eXo.OpenAjax.portlet = {
	init : function(id) {
		this.id = id;

		this.container = new OpenAjax.hub.InlineContainer( eXo.OpenAjax.hub, this.id,
                {   Container: {
                        onSecurityAlert: function( source, alertType ) {
                            console.log( "onSecurityAlert: s=" + source.getClientID() + " a=" + alertType );
                        },
                        scope: this,
                        log: function( msg ) { console.log( msg ); }
                    }
                });

		this.hubClient = new OpenAjax.hub.InlineHubClient({
		        HubClient: {
		            onSecurityAlert: function( source, alertType ) {
		                console.log( "onSecurityAlert: s=" + source.getClientID() + " a=" + alertType );
		            },
		            scope: this,
		            log: function(msg) { console.log( msg ); }
		        },
		        InlineHubClient: {
		            container: this.container
		        }
		});

		this.hubClient.connect();
	}
};
