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
        record = table.get_item(Key={'Id': quote['id'] }, ConsistentRead=True)
        item = record.get('Item', { 'Id': quote['id'], 'Quotes': [] } )
        logger.info(item)
        item['Quotes'].append( { 'bankId': quote['bankId'], 'rate': "%.2f" % quote['rate'] }) 
        logger.info(item)
        table.put_item(Item = item)
        
        if complete(item):
          logger.info('Aggregate complete')
          try:
            sfn.send_task_success(taskToken=item['Id'], output=json.dumps(item['Quotes']))  
          except ClientError as ce:
            logger.info('Could not complete workflow %s' % item['Id'])
            logger.info(ce)
    return 0
