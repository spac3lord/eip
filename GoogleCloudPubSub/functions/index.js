const Pubsub = require('@google-cloud/pubsub');
const pubsub = Pubsub({projectId: "eaipubsub"})

/**
 * Content-based Router Cloud Function.
 *
 * www.EnterpriseIntegrationPatterns.com
 * 
 * @param {object} event The Cloud Functions event.
 */
exports.contentBasedRouter = function contentBasedRouter(event) {
  // extract data from message
  const pubsubMessage = event.data;
  const payload = Buffer.from(pubsubMessage.data, 'base64').toString();
  console.log("Payload: " + payload);
  order = JSON.parse(payload)

  // determine target channel 
  outChannel = getOutChannel(order.type);
  console.log("Publishing to: " + outChannel)

  // forward the message
  return pubsub.topic(outChannel).get({autoCreate: true}).then(function(data) {
    var topic = data[0];
    return topic.publish(order);
  })
};

function getOutChannel(type) {
  switch(type) {
    case "widget": 
      return "widgets";
    case "gadget": 
      return "gadgets";
    default: 
      return "unknown";
  }
}
