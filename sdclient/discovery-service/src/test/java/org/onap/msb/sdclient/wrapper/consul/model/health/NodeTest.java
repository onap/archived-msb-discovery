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

public class NodeTest {
	@Test
	public void testImmutableNode() {
		ImmutableNode node0 = ImmutableNode.builder().address("10.74.151.26")
				.node("test").build();
		Assert.assertEquals("10.74.151.26",node0.getAddress());
		Assert.assertEquals("test",node0.getNode());
	

		ImmutableNode node1 = node0.withAddress("10.74.151.27").withNode("test2");

		Assert.assertFalse(node0.equals(node1));

		System.out.println(node1.hashCode());

		ImmutableNode node2 = ImmutableNode.builder().from(node1)
				.build();
		Assert.assertEquals("10.74.151.27", node2.getAddress());
		
		ImmutableNode node3 = ImmutableNode.copyOf(node1);
		Assert.assertTrue(node3.equals(node1));
	}
	
	@Test
	public void testtoString() {
		ImmutableNode node0 = ImmutableNode.builder().address("10.74.151.26")
				.node("test").build();
		String nodeInfo="Node{node=test, address=10.74.151.26}";
		Assert.assertEquals(nodeInfo, node0.toString());
	}
}
