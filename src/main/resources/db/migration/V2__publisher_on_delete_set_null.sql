
/*
Set the publisher_id to NULL when the referenced publisher is deleted

if we would need more control, we could remove it from db logic and manage it in the application:

// book repository
@Modifying
@Query("UPDATE Book b SET b.publisher = NULL WHERE b.publisher.id = :publisherId")
int nullifyPublisher(@Param("publisherId") UUID publisherId);

// publisher service
@Transactional
public void deletePublisher(UUID id) {
    if (!publisherRepository.existsById(id)) {
        throw new ResourceNotFoundException("Publisher not found with id: " + id);
    }
    bookRepository.nullifyPublisher(id);
    publisherRepository.deleteById(id);
}
*/

ALTER TABLE book DROP CONSTRAINT fk_book_publisher;

ALTER TABLE book ADD CONSTRAINT fk_book_publisher
    FOREIGN KEY (publisher_id) REFERENCES publisher(id)
        ON DELETE SET NULL;