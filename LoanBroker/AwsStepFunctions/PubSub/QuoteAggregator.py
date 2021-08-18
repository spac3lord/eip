import boto3
import json
import logging
import decimal
from botocore.exceptions import ClientError

logger = logging.getLogger()
logger.setLevel(logging.INFO)
dynamo = boto3.resource('dynamodb')
sfn = boto3.client('stepfunctions')

def complete(item):
    return len(item['Quotes']) >= 2

def lambda_handler(event, context):
    logger.info(event)

    for record in event['Records']:
        quote = json.loads(record['body'])
        logger.info(quote)
        table = dynamo.Table('MortgageQuotes')
        try:
          (key, token) = quote['id'].split("::")
        except ValueError: 
          logger.error("Invalid quote id %s" % quote['id'])
          return 0
        record = table.get_item(Key={'Id': key }, ConsistentRead=True)
        item = record.get('Item', { 'Id': key, 'Quotes': [], 'Token': token } )
        logger.info(item)
        item['Quotes'].append( { 'bankId': quote['bankId'], 'rate': "%.2f" % quote['rate'] }) 
        logger.info(item)
        table.put_item(Item = item)
        
        if complete(item):
          logger.info('Aggregate complete: %s' % token)
          try:
            sfn.send_task_success(taskToken=token, output=json.dumps(item['Quotes']))  
          except ClientError as ce:
            logger.info('Could not complete workflow %s' % item['Id'])
            logger.info(ce)
    return 0
