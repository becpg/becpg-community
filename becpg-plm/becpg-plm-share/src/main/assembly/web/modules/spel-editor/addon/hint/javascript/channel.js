var hints = {
  "query": "",
  "dateFilter": {
    "dateFilterField": "cm:modified",
    "dateFilterType": "Before, After, From, To, Equals",
    "dateFilterDelay": "1",
    "dateFilterDelayUnit": "Min, Hour, Day"
  },
  "versionFilter": {
    "versionFilterType": "MAJOR, MINOR, NONE"
  },
  "entityFilter": {
    "entityType": "bcpg:semiFinishedProduct",
    "criteria": {
      "assoc_bcpg_plants_added": "nodeRef"
    }
  },
  "properties": {
    "connector.notify.enabled": "true",
    "connector.notify.from": "support@becpg.fr",
    "connector.notify.to": "support@becpg.fr",
    "remote.extra.fields": "cm:titled,cm:description",
    "remote.extra.lists": "bcpg:compoList"
  },
  "entity": {
    "datalists": {
      "bp:pubChannelList": [
        {
          "type": "bp:pubChannelList",
          "attributes": {
            "bp:pubChannellListStatus": "SUCCESS",
            "bp:pubChannellListError": "",
            "bp:pubChannelListBatchId": "1"
          },
          "bp:pubChannelListChannel": {
            "path": "/app:company_home/cm:System/cm:Characts/bcpg:entityLists/cm:PubChannels",
            "type": "bp:pubChannel",
            "bp:pubChannelId": "sample-canal"
          }
        }
      ]
    },
    "documents": [],
    "path": "/app:company_home/cm:System/cm:Characts/bcpg:entityLists/cm:PubChannels",
    "cm:name": "Sample canal",
    "attributes": {
      "bp:pubChannelId": "sample-canal",
      "bp:pubChannelConfig": "...",
      "bp:pubChannelBatchDuration": "null",
      "bp:pubChannelBatchId": "1",
      "bp:pubChannelStatus": "STARTED",
      "bp:pubChannelBatchEndTime": "1672320614749",
      "bp:pubChannelBatchStartTime": "1672320000652",
      "bp:pubChannelLastDate": "1672320540452",
      "bp:pubChannelAction": "null",
      "bp:pubChannelFailCount": "1",
      "bp:pubChannelError": "",
      "bp:pubChannelLastSuccessBatchId": "1",
      "bp:pubChannelReadCount": "1"
    },
    "type": "bp:pubChannel",
    "bp:pubChannelId": "sample-canal"
  }
};