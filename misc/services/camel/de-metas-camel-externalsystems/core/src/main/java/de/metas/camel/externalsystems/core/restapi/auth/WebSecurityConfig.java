/*
 * #%L
 * de-metas-camel-externalsystems-core
 * %%
 * Copyright (C) 2021 metas GmbH
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

package de.metas.camel.externalsystems.core.restapi.auth;

import com.sun.istack.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import static de.metas.camel.externalsystems.common.ExternalSystemCamelConstants.REST_WOOCOMMERCE_PATH;
import static de.metas.camel.externalsystems.common.ExternalSystemCamelConstants.WOOCOMMERCE_AUTHORITY;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter
{
	private final TokenAuthProvider tokenAuthProvider;

	public WebSecurityConfig(@NotNull final TokenAuthProvider tokenAuthProvider)
	{
		this.tokenAuthProvider = tokenAuthProvider;
	}

	@Override
	protected void configure(@NotNull final HttpSecurity http) throws Exception
	{
		//@formatter:off
		http.authenticationProvider(tokenAuthProvider)
				.csrf()
				  .disable()
				.authorizeRequests()
				  .antMatchers("/**" + REST_WOOCOMMERCE_PATH).hasAuthority(WOOCOMMERCE_AUTHORITY)
				  .anyRequest()
				  .authenticated();
		//@formatter:on

		http.addFilterBefore(new AuthenticationFilter(authenticationManager()), BasicAuthenticationFilter.class);
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManager() throws Exception
	{
		return super.authenticationManagerBean();
	}
}
