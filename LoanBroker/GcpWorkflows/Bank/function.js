
const {PubSub} = require('@google-cloud/pubsub');
const pubSub = new PubSub();

function calcRate(amount, term, score, history) {
  if (amount <= process.env.MAX_LOAN_AMOUNT && score >= process.env.MIN_CREDIT_SCORE) {
   return parseFloat(process.env.BASE_RATE) + Math.random() * ((1000 - score) / 100.0);
  }
}

exports.bank = async (message, context) => {
    const request = message.data ? Buffer.from(message.data, 'base64').toString() : '{}';
    console.log(message.attributes);
    const requestId = message.attributes["RequestId"];
    console.log(request);
    const quoteRequest = JSON.parse(request); 
    const bankId = process.env.BANK_ID;
    const responseTopic = process.env.QUOTE_RESP_TOPIC;

    response = bankQuote(quoteRequest, bankId);
    
    const dataBuffer = Buffer.from(JSON.stringify(response));
    const customAttributes = { id: requestId };  
    const messageId = await pubSub.topic(responseTopic).publish(dataBuffer, customAttributes);
    console.log(`Message ${messageId} published.`);
}

bankQuote = (quoteRequest, bankId) => {
    console.log('Loan Request over %d at credit score %d', quoteRequest.Amount, quoteRequest.Credit.Score);
    const rate = calcRate(quoteRequest.Amount, quoteRequest.Term, quoteRequest.Credit.Score, quoteRequest.Credit.History);

    if (rate) {
        console.log('%s offering Loan at %f', bankId, rate);
        return { "rate": rate, "bankId": bankId };
    } else {
        console.log('%s rejecting Loan', bankId);
        return {};
    }
}
