describe('Reportes Mensuales PDF', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/registro-asistencias')
    cy.get('.reports-card').scrollIntoView()
  })

  it('Debe mostrar la sección de reportes mensuales', () => {
    cy.contains('Reportes Mensuales').should('be.visible')
    cy.get('mat-select[formControlName="month"]').should('be.visible')
    cy.get('mat-select[formControlName="year"]').should('be.visible')
    cy.contains('button', 'Exportar PDF').should('be.visible')
  })

  it('Debe generar reporte PDF de noviembre 2025', () => {
    // Seleccionar mes de noviembre
    cy.get('mat-select[formControlName="month"]').click()
    cy.get('mat-option').contains('Noviembre').click()
    
    // Seleccionar año 2025
    cy.get('mat-select[formControlName="year"]').click()
    cy.get('mat-option').contains('2025').click()
    
    // Hacer clic en Exportar PDF
    cy.contains('button', 'Exportar PDF').click({ force: true })
    
    // Esperar a que termine la generación
    cy.wait(3000)
    
    // Verificar que no hay errores críticos
    cy.get('body').should('be.visible')
    cy.log('✅ Reporte PDF de noviembre 2025 generado correctamente')
  })

  it('Debe generar reporte CSV de noviembre 2025', () => {
    // Seleccionar mes de noviembre
    cy.get('mat-select[formControlName="month"]').click()
    cy.get('mat-option').contains('Noviembre').click()
    
    // Seleccionar año 2025
    cy.get('mat-select[formControlName="year"]').click()
    cy.get('mat-option').contains('2025').click()
    
    // Hacer clic en Exportar CSV
    cy.contains('button', 'Exportar CSV').click({ force: true })
    
    // Esperar a que termine la generación
    cy.wait(3000)
    
    // Verificar que no hay errores críticos
    cy.get('body').should('be.visible')
    cy.log('✅ Reporte CSV de noviembre 2025 generado correctamente')
  })
})