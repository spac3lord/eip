function calcRate(amount, term, score, history) {
  if (amount <= process.env.MAX_LOAN_AMOUNT && score >= process.env.MIN_CREDIT_SCORE) {
      return parseFloat(process.env.BASE_RATE) + Math.random() * ((1000 - score) / 100.0);
    }
}

exports.handler = async (event, context) => {
    console.log(event.Records[0].Sns);
    const message = event.Records[0].Sns.Message;
    const requestId = event.Records[0].Sns.MessageAttributes.RequestId.Value;
    const data = JSON.parse(message);
    const bankId = process.env.BANK_ID;
    
    console.log('Loan Request over %d at credit score %d', data.Amount, data.Credit.Score);
    const rate = calcRate(data.Amount, data.Term, data.Credit.Score, data.Credit.History);


    if (rate) {
        const quote = { "rate": rate, "bankId": bankId, "id": requestId};
        console.log('Offering Loan', quote);
        return quote;
    } else {
        console.log('Rejecting Loan');
    }
};
