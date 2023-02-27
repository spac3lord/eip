import json
import boto3

dynamo = boto3.resource('dynamodb')
table_name = 'Entities'

def lambda_handler(event, context):
    print ("# events %i" % len(event))
    keys = []
    for evt in event:
      body = json.loads(evt['body'])
      keys += [ {'id': body['id']} ]
    print ("Looking up entities for %s" % keys)
    data = dynamo.batch_get_item(RequestItems={table_name: {'Keys': keys } })
    if table_name in data['UnprocessedKeys']:
      print ("Unprocessed items:" % data['UnprocessedKeys'][table_name])
    entities = data['Responses'][table_name]
    print (entities)
    return entities