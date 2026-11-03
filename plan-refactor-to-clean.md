This project is created in a spring Classical layered architecture:
```
Presentation (controllers, DTOs)
    |
    v
Service (business logic, workflows)
    |
    v
Repository (Spring Data interface, extends JpaRepository)
    |
    v
Database
```


I would like to transform it to a clean architecture:
```
Presentation (BookController, BookDto)
    |
    | calls
    v
Application (Use cases)
    |
    | uses
    v
Domain (Book, BookRepository interface)
    ^
    | implements
    |
Infrastructure
    |
    +--> BookRepositoryImpl (adapter)
            |
            v
SpringDataBookRepository (extends JpaRepository)
            |
            v
        Database
        
```

Choices:
- Domain purity: Pragmatic: JPA on domain models (Not using Pure option)
- Use case style: Interface per operation (Recommended)
- Class layout: One service class per aggregate (Recommended)

What crosses the Application ↔ Presentation boundary? 
- 1.Commands in, domain out 
- 2.DTOs cross the boundary 

Using option 2, because:
- Both options keep the rule that matters most: domain entities never cross the boundary.
  Controllers don’t touch Book, so you avoid lazy-loading surprises, accidental exposure of fields, 
  and web code calling domain behavior.
- Option 1’s extra ceremony (six command records and three mappers) mostly copies fields from one shape to another.
- Option 1 also has a quiet weakness: returning the Book domain object makes the controller depend on the 
  domain’s getters, so changing the domain can break the web layer. 

When to pick option 1 instead: You’re going for pure Clean Architecture with the application layer fully framework-free.

---

## As implemented (corrected graph)

Two details in the sketch above differ from the final implementation:
- DTOs live in Application, not Presentation (per the "DTOs cross the boundary" choice)
- `PublishBookUseCase` does not exist; each operation has its own `*UseCase` interface

```
Presentation
    |
    | controllers call *UseCase interfaces, import DTOs from Application
    v
Application
    |
    | *UseCase interfaces (one per operation, implemented by *Service)
    | DTOs and exceptions live here
    |
    | uses
    v
Domain
    |
    entities (JPA-annotated) + *Repository port interfaces (plain Java)
    ^
    | implements
    |
Infrastructure
    |
    +--> BookRepositoryImpl (adapter)
            |
            v
        SpringDataBookRepository (extends JpaRepository)
            |
            v
        Database
```

Dependency rules that hold in the code:
- web -> application (use case interfaces and DTOs), never directly to domain or infrastructure
- application -> domain (entities and ports), never to infrastructure
- infrastructure -> domain (implements the ports), never to application or web

**Important** \
the business logic did not move to the domain. \
The refactor changed the dependency structure (ports, adapters, use case interfaces), 
but the behavior stayed where it was, in the services. 

The current domain layer is what clean-architecture purists would call an anemic domain model: 
entities that are pure data bags (Lombok getters/setters + JPA annotations) with zero behavior.