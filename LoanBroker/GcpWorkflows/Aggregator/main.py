from google.cloud import datastore
import google.cloud.logging
import google.auth
import google.auth.transport.requests

creds, project = google.auth.default()
db = datastore.Client()
log_client= google.cloud.logging.Client()
log_client.setup_logging()

import logging
import requests
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

def completion_callback(correlationId, aggregate):
    logging.info("Aggregate complete: %s", correlationId)
    url = correlationId

    auth_req = google.auth.transport.requests.Request()
    creds.refresh(auth_req)
    status = requests.post(url, json = aggregate, headers = { "Authorization": f"Bearer {creds.token}" } )
    logging.info("Sent Callback: %i", status.status_code)

def handle_quote(event, context):
    if not 'data' in event:
        return 0
    
    data = base64.b64decode(event['data']).decode('utf-8')
    quote = json.loads(data)
    requestId = event['attributes']['id']
    logging.info("%s %s", quote, requestId)

    # Message Filter
    if not 'bankId' in quote:
        logging.warning("incomplete quote received")
        return 0

    # Aggregator
    item = store_quote(requestId, quote)
    if is_complete(item):
      completion_callback(requestId, item)  
    return 0
