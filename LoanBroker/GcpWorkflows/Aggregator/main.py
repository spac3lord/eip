from google.cloud import datastore
import google.cloud.logging

db = datastore.Client()
log_client= google.cloud.logging.Client()
log_client.setup_logging()
import logging
import base64
import json

def is_complete(item):
    return len(item['Quotes']) >= 2

def store_quote(correlationId, quote):
    key = db.key("MortgageQuotes", correlationId)
    with db.transaction():
        item = db.get(key)
        if not item:
            item = datastore.Entity(key)
            item['Quotes'] = []
        item['Quotes'].append( { 'bankId': quote['bankId'], 'rate': quote['rate'] }) 
        db.put(item)
    logging.info(item)
    return item


def handle_quote(event, context):
    if not 'data' in event:
        return 0
    
    data = base64.b64decode(event['data']).decode('utf-8')
    quote = json.loads(data)
    requestId = event['attributes']['id']
    logging.info("%s %s", quote, requestId)

    item = store_quote(requestId, quote)
    if is_complete(item):
        logging.info("Aggregate %s complete", requestId)
    return 0
