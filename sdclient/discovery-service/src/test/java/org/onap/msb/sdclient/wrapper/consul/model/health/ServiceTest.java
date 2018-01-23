/**
 * Copyright 2016-2017 ZTE, Inc. and others.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onap.msb.sdclient.wrapper.consul.model.health;

import org.junit.Assert;
import org.junit.Test;

public class ServiceTest {
	@Test
	public void testImmutableService() {
		ImmutableService service0 = ImmutableService.builder()
				.id("huangleibo_id").port(0).address("").service("huangleibo")
				.addTags("111", "222").addTags("333").build();
		Assert.assertEquals("huangleibo_id", service0.getId());
	

		ImmutableService service1 = service0.withId("huangleibo_id")
				.withId("new_id").withService("huangleibo")
				.withService("new_service").withTags("new_tags")
				.withAddress("").withAddress("new_address").withPort(0)
				.withPort(1);

		Assert.assertFalse(service0.equals(service1));

		System.out.println(service1.hashCode());

		ImmutableService service2 = ImmutableService.builder().from(service1)
				.build();
		Assert.assertEquals("new_id", service2.getId());
		
		ImmutableService service3 = ImmutableService.copyOf(service2);
		Assert.assertTrue(service3.equals(service2));
	}
	
	@Test
	public void testtoString() {
		ImmutableService service0 = ImmutableService.builder()
				.id("huangleibo_id").port(0).address("").service("huangleibo")
				.addTags("111", "222").build();
		String nodeInfo="Service{id=huangleibo_id, service=huangleibo, tags=[111, 222], address=, port=0}";
		Assert.assertEquals(nodeInfo, service0.toString());
	}
}
