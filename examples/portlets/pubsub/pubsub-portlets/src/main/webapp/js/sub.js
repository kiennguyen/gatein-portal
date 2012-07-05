var subId;

$("body").on("click", ".jz .subscribe", function() {
  subId = eXo.OpenAjax.portlet.hubClient.subscribe("org.apache.shindig.random-number", callback);
});

$("body").on("click", ".jz .unsubscribe", function() {
  eXo.OpenAjax.portlet.hubClient.unsubscribe(subId);
  $(this).closest().find("data").val("");
});

function callback(topic, data, subscriberData) {
  $(this).closest().find("data").val(
    "message : " + data + "<br/>" +
    "received at: " + (new Date()).toString());
}