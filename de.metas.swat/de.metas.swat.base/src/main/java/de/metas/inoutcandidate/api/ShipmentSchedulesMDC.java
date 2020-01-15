package de.metas.inoutcandidate.api;

import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;

import de.metas.inoutcandidate.model.I_M_ShipmentSchedule;
import de.metas.process.PInstanceId;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/*
 * #%L
 * de.metas.swat.base
 * %%
 * Copyright (C) 2020 metas GmbH
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

@UtilityClass
public class ShipmentSchedulesMDC
{
	public static MDCCloseable withShipmentScheduleId(@NonNull final ShipmentScheduleId shipmentScheduleId)
	{
		return MDC.putCloseable(I_M_ShipmentSchedule.COLUMNNAME_M_ShipmentSchedule_ID, Integer.toString(shipmentScheduleId.getRepoId()));
	}

	public static MDCCloseable withShipmentScheduleUpdateRunNo(final int runNo)
	{
		return MDC.putCloseable("ShipmentScheduleUpdater-Run#", Integer.toString(runNo));
	}

	public static MDCCloseable withRevalidationId(@NonNull final PInstanceId selectionId)
	{
		return MDC.putCloseable("AD_PInstance_ID", Integer.toString(selectionId.getRepoId()));
	}
}