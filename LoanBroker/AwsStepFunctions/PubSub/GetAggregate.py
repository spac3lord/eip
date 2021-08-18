import boto3
import json

import logging
logger = logging.getLogger()
logger.setLevel(logging.INFO)

dynamo = boto3.resource('dynamodb')

def lambda_handler(event, context):
  logger.info(event)
  key = event['Id']
  table = dynamo.Table('MortgageQuotes')
  record = table.get_item(Key={'Id': key }, ConsistentRead=True)
  if 'Item' in record:
    return {'Quotes' : record['Item']['Quotes'] }
  else:
    logger.info("No aggregate for key %s" % key)
    return {'Quotes' : [] }
