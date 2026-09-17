# Instrucciones para el desarrollo de componentes visuales

- Se deben crear los componentes visuales en archivos HTML separados dentro de la carpeta `src/main/resources/templates/TestTemplate/`. Esto permite probar y desarrollar los componentes de manera aislada antes de integrarlos en la aplicación principal.

- Los componentes utilizan Thymeleaf como motor de plantillas para renderizar dinámicamente los datos en el frontend. Se deben seguir las convenciones de Thymeleaf para enlazar los datos y manejar la lógica de presentación.

- Se debe tener en cuenta el modelo de datos y las relaciones entre entidades al desarrollar los componentes visuales, asegurando que la información se muestre de manera coherente y precisa.

- Se debe buscar un estilo visual minimalista y consistente con la estética general de la aplicación. Se recomienda utilizar Bootstrap 5 para facilitar el diseño y la maquetación de los componentes.

- Se debe desarrollar con el principio de "mobile-first", asegurando que los componentes se vean y funcionen correctamente en dispositivos móviles antes de adaptarlos a pantallas más grandes.

- Evitar el uso de texto verboso o innecesario en los componentes. Se debe priorizar la claridad y la concisión en la presentación de la información.

- Evitar la estilizacion tipica generada por herramientas de Inteligencia Artificial como bordes excesivamente redondeados, exceso en el uso de sombras y degradados. Se debe buscar un diseño limpio y profesional. Utilizar lo estrictamente necesario para lograr un diseño atractivo y funcional. (Puedes tener cierta libertad creativa, pero siempre priorizando la simplicidad y la coherencia con el estilo general de la aplicación, no seas prohibitivo siempre y cuando consideres que es lo mejor para la experiencia del usuario).

- Se debe evitar el uso de colores chillones o poco armoniosos. Se recomienda utilizar una paleta de colores coherente con la identidad visual de la aplicación y que sea agradable a la vista.

- La convencion de nombres para las clases que no pertenezcan a Bootstrap son en kebab-case, para mantener consistencia y claridad en el código. Los nombres deben hacer referencia al componente o funcionalidad que representan, evitando abreviaturas confusas o nombres genéricos.
    Ejemplo: `user-profile-card`, `product-list-item`, `order-summary-table`.
