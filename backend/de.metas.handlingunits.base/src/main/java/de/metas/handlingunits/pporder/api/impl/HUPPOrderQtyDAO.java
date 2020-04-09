package de.metas.handlingunits.pporder.api.impl;

import static org.adempiere.model.InterfaceWrapperHelper.newInstance;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.util.proxy.Cached;
import org.adempiere.warehouse.LocatorId;
import org.compiere.util.Env;
import org.compiere.util.TimeUtil;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import de.metas.cache.annotation.CacheCtx;
import de.metas.cache.annotation.CacheTrx;
import de.metas.handlingunits.HuId;
import de.metas.handlingunits.exceptions.HUException;
import de.metas.handlingunits.model.I_PP_Order_Qty;
import de.metas.handlingunits.picking.PickingCandidateId;
import de.metas.handlingunits.pporder.api.CreateIssueCandidateRequest;
import de.metas.handlingunits.pporder.api.CreateReceiptCandidateRequest;
import de.metas.handlingunits.pporder.api.IHUPPOrderQtyDAO;
import de.metas.material.planning.pporder.PPOrderBOMLineId;
import de.metas.material.planning.pporder.PPOrderId;
import de.metas.quantity.Quantity;
import de.metas.util.Services;
import lombok.NonNull;

/*
 * #%L
 * de.metas.handlingunits.base
 * %%
 * Copyright (C) 2017 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

public class HUPPOrderQtyDAO implements IHUPPOrderQtyDAO
{
	@Override
	public I_PP_Order_Qty retrieveById(final int ppOrderQtyId)
	{
		Preconditions.checkArgument(ppOrderQtyId > 0, "ppOrderQtyId > 0");

		return InterfaceWrapperHelper.load(ppOrderQtyId, I_PP_Order_Qty.class);
	}

	@Override
	public List<I_PP_Order_Qty> saveAll(@NonNull final Collection<CreateReceiptCandidateRequest> requests)
	{
		return requests.stream()
				.map(this::save)
				.collect(ImmutableList.toImmutableList());
	}

	@Override
	public I_PP_Order_Qty save(@NonNull final CreateReceiptCandidateRequest request)
	{
		final I_PP_Order_Qty record = newInstance(I_PP_Order_Qty.class);
		record.setPP_Order_ID(request.getOrderId().getRepoId());
		record.setPP_Order_BOMLine_ID(PPOrderBOMLineId.toRepoId(request.getOrderBOMLineId()));
		record.setAD_Org_ID(request.getOrgId().getRepoId());
		record.setMovementDate(TimeUtil.asTimestamp(request.getDate()));
		record.setM_Locator_ID(request.getLocatorId().getRepoId());
		record.setM_HU_ID(request.getTopLevelHUId().getRepoId());
		record.setM_Product_ID(request.getProductId().getRepoId());
		record.setQty(request.getQtyToReceive().toBigDecimal());
		record.setC_UOM_ID(request.getQtyToReceive().getUomId().getRepoId());
		record.setProcessed(false);
		record.setM_Picking_Candidate_ID(PickingCandidateId.toRepoId(request.getPickingCandidateId()));
		save(record);

		return record;
	}

	@Override
	public I_PP_Order_Qty save(@NonNull final CreateIssueCandidateRequest request)
	{
		final I_PP_Order_Qty record = newInstance(I_PP_Order_Qty.class);

		record.setPP_Order_ID(request.getOrderId().getRepoId());
		record.setPP_Order_BOMLine_ID(request.getOrderBOMLineId().getRepoId());

		record.setM_Locator_ID(LocatorId.toRepoId(request.getLocatorId()));
		record.setM_HU_ID(request.getIssueFromHUId().getRepoId());
		record.setM_Product_ID(request.getProductId().getRepoId());

		final Quantity qtyToIssue = request.getQtyToIssue();
		record.setQty(qtyToIssue.toBigDecimal());
		record.setC_UOM_ID(qtyToIssue.getUomId().getRepoId());

		record.setMovementDate(TimeUtil.asTimestamp(request.getDate()));
		record.setProcessed(false);

		record.setM_Picking_Candidate_ID(PickingCandidateId.toRepoId(request.getPickingCandidateId()));

		save(record);

		return record;
	}

	@Override
	public void save(final I_PP_Order_Qty ppOrderQty)
	{
		InterfaceWrapperHelper.save(ppOrderQty);
	}

	@Override
	public void delete(final I_PP_Order_Qty ppOrderQty)
	{
		InterfaceWrapperHelper.delete(ppOrderQty);
	}

	@Override
	public List<I_PP_Order_Qty> retrieveOrderQtys(final PPOrderId ppOrderId)
	{
		return retrieveOrderQtys(Env.getCtx(), ppOrderId, ITrx.TRXNAME_ThreadInherited);
	}

	@Cached(cacheName = I_PP_Order_Qty.Table_Name + "#by#PP_Order_ID", expireMinutes = 10)
	List<I_PP_Order_Qty> retrieveOrderQtys(@CacheCtx final Properties ctx, @NonNull final PPOrderId ppOrderId, @CacheTrx final String trxName)
	{
		return Services.get(IQueryBL.class)
				.createQueryBuilder(I_PP_Order_Qty.class, ctx, trxName)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(I_PP_Order_Qty.COLUMN_PP_Order_ID, ppOrderId)
				.create()
				.listImmutable(I_PP_Order_Qty.class);
	}

	@Override
	public I_PP_Order_Qty retrieveOrderQtyForCostCollector(@NonNull final PPOrderId ppOrderId, final int costCollectorId)
	{
		Preconditions.checkArgument(costCollectorId > 0, "costCollectorId shall be > 0");

		return retrieveOrderQtys(ppOrderId)
				.stream()
				.filter(cand -> cand.getPP_Cost_Collector_ID() == costCollectorId)
				// .peek(cand -> Check.assume(cand.isProcessed(), "Candidate was expected to be processed: {}", cand))
				.reduce((cand1, cand2) -> {
					throw new HUException("Expected only one candidate but got: " + cand1 + ", " + cand2);
				})
				.orElse(null);
	}

	@Override
	@Cached(cacheName = I_PP_Order_Qty.Table_Name + "#by#" + I_PP_Order_Qty.COLUMNNAME_M_HU_ID)
	public boolean isHuIdIssued(@NonNull final HuId huId)
	{
		return Services.get(IQueryBL.class).createQueryBuilder(I_PP_Order_Qty.class)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(I_PP_Order_Qty.COLUMN_M_HU_ID, huId)
				.addNotNull(I_PP_Order_Qty.COLUMN_PP_Order_BOMLine_ID) // it's actually an issue (and not a receipt)
				.create()
				.anyMatch();
	}
}