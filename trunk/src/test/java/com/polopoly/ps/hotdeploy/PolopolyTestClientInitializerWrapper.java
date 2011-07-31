package com.polopoly.ps.hotdeploy;

import com.polopoly.ps.test.client.ClientInitializer;
import com.polopoly.ps.test.client.PolopolyTestClientInitializer;
import com.polopoly.ps.test.service.ServiceWrapper;
import com.polopoly.ps.test.service.ServiceWrapperRelativeOrder;
import com.polopoly.util.client.PolopolyClient;

public class PolopolyTestClientInitializerWrapper extends
		PolopolyTestClientInitializer implements
		ServiceWrapper<ClientInitializer> {
	@Override
	protected void configureClient(PolopolyClient client) {
		client.setAttachSolrSearchClient(false);
		client.setAttachSearchService(false);
	}

	@Override
	public void setDelegate(ClientInitializer service) {
		// we just discard it.
	}

	@Override
	public ServiceWrapperRelativeOrder getWrapperOrder(
			ServiceWrapper<ClientInitializer> otherWrapper) {
		return ServiceWrapperRelativeOrder.I_DONT_CARE;
	}

	@Override
	public int getWrapperIndex() {
		return Integer.MAX_VALUE;
	}
}
