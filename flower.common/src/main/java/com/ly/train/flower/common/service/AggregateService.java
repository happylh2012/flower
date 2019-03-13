/**
 * Copyright © ${project.inceptionYear} 同程艺龙 (zhihui.li@ly.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ly.train.flower.common.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.ly.train.flower.common.service.container.ServiceContext;
import com.ly.train.flower.common.service.message.FlowMessage;

public class AggregateService implements Service<Object>, Aggregate {

  int sourceNumber = 0;

  // <messageId,Set<message>>
  Map<String, Set<Object>> resultMap = new ConcurrentHashMap<String, Set<Object>>();
  // <messageId,sourceNumber>
  Map<String, Integer> resultNumberMap = new ConcurrentHashMap<String, Integer>();

  @Override
  public Object process(Object message, ServiceContext context) {

    FlowMessage flowMessage = (FlowMessage) message;

    // first joint message
    if (!resultMap.containsKey(flowMessage.getTransactionId())) {
      Set<Object> objectSet = new HashSet<Object>();
      resultMap.put(flowMessage.getTransactionId(), objectSet);
      resultNumberMap.put(flowMessage.getTransactionId(), sourceNumber);
    }
    resultMap.get(flowMessage.getTransactionId()).add(((FlowMessage) message).getMessage());

    Integer integer = resultNumberMap.get(flowMessage.getTransactionId()) - 1;
    resultNumberMap.put(flowMessage.getTransactionId(), integer);
    if (integer <= 0) {
      Set<Object> returnObject = resultMap.get(flowMessage.getTransactionId());
      resultMap.remove(flowMessage.getTransactionId());
      resultNumberMap.remove(flowMessage.getTransactionId());

      return buildMessage(returnObject);
    }
    // TODO resultNumberMap&resultMap memory leak
    return null;
  }

  /**
   * subclass should override the method.
   * @param messages: Set<Message>
   * @return
   */
  public Object buildMessage(Set<Object> messages) {
    return messages;
  }

  @Override
  // sourceNumber++ when initialize
  public void setSourceNumber(int number) {
    sourceNumber = number;
  }

}
