const functions = require('@google-cloud/functions-framework');

function getRandomInt(min, max) {
  return min + Math.floor(Math.random() * (max-min));
}

// Expects a SSN to return a random credit score. Passes back RequestId.
exports.creditBureau = (req, res) => {
  const min_score = 300
  const max_score = 900

  var ssn_regex = new RegExp("^\\d{3}-\\d{2}-\\d{4}$");
  if (ssn_regex.test(req.query.SSN)) {
    res.status(200).send(JSON.stringify(
      {
        statusCode: 200,
        request_id: req.query.RequestId,
        body: {
          SSN: req.query.SSN,
          score: getRandomInt(min_score, max_score),
          history: getRandomInt(1,30)
        }
      }
    ));
  } else {
    res.status(400).send(JSON.stringify(
      {
        request_id: req.query.RequestId,
        body: {
          SSN: req.query.SSN
        }
      }
    ));
  }
};
