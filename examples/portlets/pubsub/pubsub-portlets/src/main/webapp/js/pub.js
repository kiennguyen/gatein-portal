eXo.OpenAjax.portlet.init("test");

$("body").on("click", ".jz .publish", function() {
  var message = Math.random();
  eXo.OpenAjax.portlet.hubClient.publish("org.apache.shindig.random-number", message);
  $(this).closest().find("data").val(message);
});