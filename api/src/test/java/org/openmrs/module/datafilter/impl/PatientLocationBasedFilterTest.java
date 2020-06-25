/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.DataFilterConstants;
import org.openmrs.module.datafilter.TestConstants;
import org.openmrs.module.datafilter.impl.api.DataFilterService;
import org.openmrs.test.TestUtil;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;

public class PatientLocationBasedFilterTest extends BaseFilterTest {
	
	@Autowired
	private PatientService patientService;
	
	@Autowired
	private DataFilterService service;
	
	@Autowired
	private SessionFactory sf;
	
	private static final String PATIENT_NAME = "Magidu";
	
	private static final String IDENTIFIER_PREFIX = "M15";
	
	private static final String TELEPHONE_AREA_CODE = "317";
	
	@Before
	public void before() {
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "patients.xml");
		updateSearchIndex();
	}
	
	@Test
	public void getAllPatients_shouldReturnPatientsAccessibleToTheUser() {
		reloginAs("dyorke", "test");
		int expCount = 3;
		Collection<Patient> patients = patientService.getAllPatients();
		assertEquals(expCount, patients.size());
		assertTrue(TestUtil.containsId(patients, 1001));
		assertTrue(TestUtil.containsId(patients, 1501));
		assertTrue(TestUtil.containsId(patients, 1503));
		
		service.grantAccess(Context.getAuthenticatedUser(), new Location(4001));
		expCount = 6;
		patients = patientService.getAllPatients();
		assertEquals(expCount, patients.size());
		assertTrue(TestUtil.containsId(patients, 1001));
		assertTrue(TestUtil.containsId(patients, 1002));
		assertTrue(TestUtil.containsId(patients, 1003));
		assertTrue(TestUtil.containsId(patients, 1501));
		assertTrue(TestUtil.containsId(patients, 1502));
		assertTrue(TestUtil.containsId(patients, 1503));
	}
	
	@Test
	public void getAllPatients_shouldReturnNoPatientsIfTheUserIsNotGrantedAccessToAnyBasis() {
		reloginAs("dBeckham", "test");
		assertEquals(0, patientService.getAllPatients().size());
	}
	
	@Test
	public void getAllPatients_shouldReturnAllPatientsIfTheAuthenticatedUserIsASuperUser() throws Exception {
		assertTrue(Context.getAuthenticatedUser().isSuperUser());
		Collection<Patient> patients = patientService.getAllPatients();
		assertEquals(11, patients.size());
		assertTrue(TestUtil.containsId(patients, 1001));
		assertTrue(TestUtil.containsId(patients, 1002));
		assertTrue(TestUtil.containsId(patients, 1003));
		assertTrue(TestUtil.containsId(patients, 1501));
		assertTrue(TestUtil.containsId(patients, 1502));
		assertTrue(TestUtil.containsId(patients, 1503));
	}
	
	@Test
	public void getPatients_shouldReturnNoPatientsIfTheUserIsNotGrantedAccessToAnyBasis() {
		Context.getAdministrationService().setGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_ANYWHERE);
		reloginAs("dBeckham", "test");
		final int expCount = 0;
		assertEquals(expCount, patientService.getCountOfPatients(PATIENT_NAME).intValue());
		assertEquals(expCount, patientService.getPatients(PATIENT_NAME).size());
		assertEquals(expCount, patientService.getCountOfPatients(IDENTIFIER_PREFIX).intValue());
		assertEquals(expCount, patientService.getPatients(IDENTIFIER_PREFIX).size());
		assertEquals(expCount, patientService.getCountOfPatients(TELEPHONE_AREA_CODE).intValue());
		assertEquals(expCount, patientService.getPatients(TELEPHONE_AREA_CODE).size());
	}
	
	@Test
	public void getPatients_shouldReturnPatientsByNameAccessibleToTheUser() {
		reloginAs("dyorke", "test");
		int expCount = 2;
		assertEquals(expCount, patientService.getCountOfPatients(PATIENT_NAME).intValue());
		Collection<Patient> patients = patientService.getPatients(PATIENT_NAME);
		assertEquals(expCount, patients.size());
		assertTrue(TestUtil.containsId(patients, 1501));
		assertTrue(TestUtil.containsId(patients, 1503));
		
		service.grantAccess(Context.getAuthenticatedUser(), new Location(4001));
		expCount = 3;
		assertEquals(expCount, patientService.getCountOfPatients(PATIENT_NAME).intValue());
		patients = patientService.getPatients(PATIENT_NAME);
		assertEquals(expCount, patients.size());
		assertTrue(TestUtil.containsId(patients, 1501));
		assertTrue(TestUtil.containsId(patients, 1502));
		assertTrue(TestUtil.containsId(patients, 1503));
	}
	
	@Test
	public void getPatients_shouldReturnPatientsByIdentifierAccessibleToTheUser() {
		reloginAs("dyorke", "test");
		int expCount = 2;
		assertEquals(expCount, patientService.getCountOfPatients(IDENTIFIER_PREFIX).intValue());
		Collection<Patient> patients = patientService.getPatients(IDENTIFIER_PREFIX);
		assertEquals(expCount, patients.size());
		assertTrue(TestUtil.containsId(patients, 1501));
		assertTrue(TestUtil.containsId(patients, 1503));
		
		service.grantAccess(Context.getAuthenticatedUser(), new Location(4001));
		expCount = 3;
		assertEquals(expCount, patientService.getCountOfPatients(IDENTIFIER_PREFIX).intValue());
		patients = patientService.getPatients(IDENTIFIER_PREFIX);
		assertEquals(expCount, patients.size());
		assertTrue(TestUtil.containsId(patients, 1501));
		assertTrue(TestUtil.containsId(patients, 1502));
		assertTrue(TestUtil.containsId(patients, 1503));
	}
	
	@Test
	public void getPatients_shouldReturnAllPatientsByIdentifierIfTheAuthenticatedUserIsASuperUser() {
		assertTrue(Context.getAuthenticatedUser().isSuperUser());
		int expCount = 4;
		assertEquals(expCount, patientService.getCountOfPatients(IDENTIFIER_PREFIX).intValue());
		Collection<Patient> patients = patientService.getPatients(IDENTIFIER_PREFIX);
		assertEquals(expCount, patients.size());
		assertTrue(TestUtil.containsId(patients, 1501));
		assertTrue(TestUtil.containsId(patients, 1502));
		assertTrue(TestUtil.containsId(patients, 1503));
		assertTrue(TestUtil.containsId(patients, 1504));
	}
	
	@Test
	public void getPatients_shouldReturnPatientsByPersonAttributeAccessibleToTheUser() {
		Context.getAdministrationService().setGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_ANYWHERE);
		reloginAs("dyorke", "test");
		int expCount = 2;
		assertEquals(expCount, patientService.getCountOfPatients(TELEPHONE_AREA_CODE).intValue());
		Collection<Patient> patients = patientService.getPatients(TELEPHONE_AREA_CODE);
		assertEquals(expCount, patients.size());
		assertTrue(TestUtil.containsId(patients, 1501));
		assertTrue(TestUtil.containsId(patients, 1503));
		
		service.grantAccess(Context.getAuthenticatedUser(), new Location(4001));
		expCount = 3;
		assertEquals(expCount, patientService.getCountOfPatients(TELEPHONE_AREA_CODE).intValue());
		patients = patientService.getPatients(TELEPHONE_AREA_CODE);
		assertEquals(expCount, patients.size());
		assertTrue(TestUtil.containsId(patients, 1501));
		assertTrue(TestUtil.containsId(patients, 1502));
		assertTrue(TestUtil.containsId(patients, 1503));
	}
	
	@Test
	public void getPatients_shouldReturnAllPatientsByPersonAttributeTypeIfTheAuthenticatedUserIsASuperUser() {
		assertTrue(Context.getAuthenticatedUser().isSuperUser());
		Context.getAdministrationService().setGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_ANYWHERE);
		int expCount = 3;
		assertEquals(expCount, patientService.getCountOfPatients(TELEPHONE_AREA_CODE).intValue());
		Collection<Patient> patients = patientService.getPatients(TELEPHONE_AREA_CODE);
		assertEquals(expCount, patients.size());
		assertTrue(TestUtil.containsId(patients, 1501));
		assertTrue(TestUtil.containsId(patients, 1502));
		assertTrue(TestUtil.containsId(patients, 1503));
	}
	
	@Test
	public void getAllPatients_shouldReturnAllPatientsIfLocationFilteringIsDisabled() {
		DataFilterTestUtils.disableLocationFiltering();
		reloginAs("dyorke", "test");
		assertEquals(11, patientService.getAllPatients().size());
	}
	
	@Test
	public void getPatients_shouldReturnAllPatientsIfLocationFilteringIsDisabled() {
		DataFilterTestUtils.disableLocationFiltering();
		reloginAs("dyorke", "test");
		int expCount = 4;
		assertEquals(expCount, patientService.getCountOfPatients(IDENTIFIER_PREFIX).intValue());
		Collection<Patient> patients = patientService.getPatients(IDENTIFIER_PREFIX);
		assertEquals(expCount, patients.size());
		assertTrue(TestUtil.containsId(patients, 1501));
		assertTrue(TestUtil.containsId(patients, 1502));
		assertTrue(TestUtil.containsId(patients, 1503));
		assertTrue(TestUtil.containsId(patients, 1504));
	}
	
	@Ignore
	@Test
	public void getAllPatients_shouldReturnAllPatientsForAUserWithTheByPassPrivilege() {
		reloginAs("dyorke", "test");
		assertEquals(3, patientService.getAllPatients().size());
		DataFilterTestUtils.addPrivilege(DataFilterConstants.PRIV_BY_PASS);
		
		assertEquals(11, patientService.getAllPatients().size());
	}
	
	@Ignore
	@Test
	public void getPatients_shouldReturnAllPatientsForAUserWithTheByPassPrivilege() {
		Context.getAdministrationService().setGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_ANYWHERE);
		reloginAs("dyorke", "test");
		int expCount = 2;
		assertEquals(expCount, patientService.getCountOfPatients(IDENTIFIER_PREFIX).intValue());
		assertEquals(expCount, patientService.getPatients(IDENTIFIER_PREFIX).size());
		expCount = 4;
		DataFilterTestUtils.addPrivilege(DataFilterConstants.PRIV_BY_PASS);
		
		assertEquals(expCount, patientService.getCountOfPatients(IDENTIFIER_PREFIX).intValue());
		assertEquals(expCount, patientService.getPatients(IDENTIFIER_PREFIX).size());
	}
	
	@Test
	public void patientFilter_shouldNotFilterPersonsThatAreNotPatients() {
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "otherPersonsThatAreNotPatients.xml");
		reloginAs("dyorke", "test");
		int expCount = 2;
		Session session = sf.getCurrentSession();
		Criteria criteria = session.createCriteria(Person.class);
		criteria.createAlias("names", "name");
		criteria.add(Restrictions.eq("name.familyName", "Katende"));
		Collection<Person> persons = criteria.list();
		assertEquals(expCount, persons.size());
		assertTrue(TestUtil.containsId(persons, 500001));
		assertTrue(TestUtil.containsId(persons, 500002));
	}
	
}
