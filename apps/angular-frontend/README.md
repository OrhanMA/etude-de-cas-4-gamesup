# AngularFrontend

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 21.1.3.

## Development server

Start the Spring API on `http://localhost:8080`, then start Angular from this directory:

```bash
npm start
```

Open `http://localhost:4200/`. The development proxy forwards `/api` and `/games` requests to Spring, so the browser uses the same origin and does not need a separate CORS configuration. The access token is held in memory and is cleared when the page reloads or when you sign out.

The client area provides `/register`, `/login`, a searchable `/catalog`, game details, and `/orders`. The protected `/admin` area provides game, client, order, author, and publisher management. Admin screens are guarded in Angular for navigation and protected again by Spring Security on the API. In production, configure the hosting server to forward `/api`, `/games`, `/authors`, and `/publishers` to Spring.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Running unit tests

To execute unit tests with the [Vitest](https://vitest.dev/) test runner, use the following command:

```bash
ng test
```

## Running end-to-end tests

For end-to-end (e2e) testing, run:

```bash
ng e2e
```

Angular CLI does not come with an end-to-end testing framework by default. You can choose one that suits your needs.

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.
