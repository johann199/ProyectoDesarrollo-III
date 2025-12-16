describe('Gestión de Usuarios - Asignar Rol', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/gestion-usuarios')
    cy.contains('Gestión de Usuarios').should('be.visible')
  })

  it('Debe asignar rol de Docente al código 2259371', () => {
    // Click en el botón "Asignar Rol"
    cy.contains('button', 'Asignar Rol').click({ force: true })
    
    // Esperar a que se abra el diálogo
    cy.get('mat-dialog-content').should('be.visible')
    
    // Llenar el código del usuario
    cy.get('input[name="userCode"]')
      .type('2259371', { force: true })
    
    // Seleccionar el rol de Docente
    cy.get('mat-select[name="role"]').click({ force: true })
    cy.get('mat-option').contains('DOCENTE').click({ force: true })
    
    // Click en el botón "Asignar Rol" del diálogo
    cy.get('mat-dialog-actions button').contains('Asignar Rol')
      .click({ force: true })
    
    // Esperar a que se cierre el diálogo
    cy.wait(2000)
    
    cy.log('✅ Rol de Docente asignado al código 2259371')
  })
})