describe('Gestión de Laboratorios - Crear y Desactivar', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/gestion-laboratorios')
    cy.contains('Gestión de Laboratorios').should('be.visible')
  })

  it('Debe crear un laboratorio con capacidad 20 y luego desactivarlo', () => {
    // Generar nombre único para poder ejecutar el test múltiples veces
    const timestamp = Date.now()
    const labName = `Lab Prueba ${timestamp}`
    
    // Crear laboratorio
    cy.log('Creando nuevo laboratorio con capacidad 20')
    
    // Llenar nombre del laboratorio
    cy.get('input[formControlName="name"]')
      .clear({ force: true })
      .type(labName, { force: true })
    
    // Llenar capacidad
    cy.get('input[formControlName="capacity"]')
      .clear({ force: true })
      .type('20', { force: true })
    
    // Click en Crear Laboratorio
    cy.contains('button', 'Crear Laboratorio').click({ force: true })
    
    // Esperar a que se procese la creación
    cy.wait(3000)
    
    // Verificar que el laboratorio aparece en la lista
    cy.get('.laboratory-table').should('be.visible')
    cy.contains(labName).should('be.visible')
    cy.contains('20 estudiantes').should('be.visible')
    
    cy.log('✅ Laboratorio creado exitosamente')
    
    // Desactivar laboratorio
    cy.log('Desactivando el laboratorio creado')
    
    // Buscar la fila del laboratorio y hacer click en Desactivar
    cy.contains('tr', labName).within(() => {
      cy.contains('button', 'Desactivar').click({ force: true })
    })
    
    // Confirmar desactivación en el diálogo
    cy.contains('Confirmar Desactivación').should('be.visible')
    cy.contains('Sí, Desactivar').click({ force: true })
    
    // Esperar a que se procese la desactivación
    cy.wait(2000)
    
    cy.log('✅ Laboratorio desactivado exitosamente')
  })
})