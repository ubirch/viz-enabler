# viz-enabler
Simple HTTP server to forward data from devices to ES

## Description

Used by Ubirch enabled devices. Send a Map[String, Double] to ElasticSearch along with the timestamp / device-id. Verifies that the device is authorized by checking against Ubirch authentication endpoint

## Data format expected
Either Json or messagePack. Example:
```json
{
  "uuid": "device-id",
  "msg_type": 0,
  "timestamp": EPOCH_SECONDS,
  "data": {
    "temperature": 33,
    "pressure": 56
  }
}

``` 
