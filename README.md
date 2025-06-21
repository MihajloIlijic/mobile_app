# FHJ Student App - Book Management

## Overview
This Android application provides a modern book management system with Firebase authentication. The landing page displays a list of books with a clean, modern TODO-style UI, and the Add Book page offers an intuitive form interface.

## Features

### Landing Page (MainActivity)
- **Modern Card-based Design**: Books are displayed in Material Design cards with rounded corners and elevation
- **Floating Action Button**: Easy access to add new books
- **Empty State**: Friendly message when no books are available
- **Real-time Updates**: Books list refreshes automatically when returning to the app
- **User-specific Actions**: Edit and delete buttons only appear for books owned by the current user

### Add Book Page (CreateBookActivity)
- **Modern Form Design**: Clean, card-based form layout with Material Design 3 components
- **Enhanced Validation**: Real-time form validation with helpful error messages
- **Status Selection**: Dropdown menu for book status (Read/Unread)
- **Visual Feedback**: Loading states and success/error messages
- **Responsive Layout**: Scrollable content with proper spacing and typography
- **Icon Integration**: Contextual icons for each form field

### Book Management
- **Add Books**: Create new books with title, author, and status
- **Edit Books**: Modify existing book information (TODO: Implementation needed)
- **Delete Books**: Remove books from the collection
- **Status Tracking**: Books have different status states (Available, Borrowed, etc.)

### Authentication
- **Firebase Auth**: Secure user authentication
- **Login/Register**: User account management
- **Password Reset**: Forgot password functionality

## UI Components

### Main Landing Page
- **Toolbar**: App title with logout button
- **RecyclerView**: Displays books in a scrollable list
- **FloatingActionButton**: Add new books
- **Empty State**: Shows when no books are available

### Book Item Card
- **Title**: Book title in bold
- **Author**: Book author name
- **Status Badge**: Current book status with colored background
- **Owner Info**: Shows book owner (truncated for privacy)
- **Action Buttons**: Edit and Delete buttons (owner only)

### Add Book Form
- **Header Section**: Icon, title, and description
- **Form Card**: Material Design card containing all input fields
- **Input Fields**: Title, Author, and Status with icons and validation
- **Save Button**: Large, prominent button with icon and loading state

## Technical Implementation

### Architecture
- **MVVM Pattern**: Using ViewModel and LiveData
- **Repository Pattern**: Centralized data access
- **Coroutines**: Asynchronous operations
- **Material Design 3**: Modern UI components

### Dependencies
- Firebase Authentication
- Material Design Components
- RecyclerView for list display
- Lifecycle components
- Kotlin Coroutines

### Form Validation
- **Real-time Validation**: Fields are validated as user types
- **Error Messages**: Clear, specific error messages for each field
- **Input Constraints**: Character limits and input types
- **Visual Feedback**: Error states with red highlighting

## Getting Started

1. **Authentication**: Login or register to access the app
2. **View Books**: The landing page shows all available books
3. **Add Books**: Tap the floating action button to add new books
4. **Fill Form**: Enter book title, author, and select status
5. **Save Book**: Tap save to add the book to your collection
6. **Manage Books**: Edit or delete your own books using the action buttons

## Form Features

### Input Fields
- **Title**: Text input with sentence capitalization, max 100 characters
- **Author**: Text input with word capitalization, max 50 characters
- **Status**: Dropdown with Read/Unread options

### Validation Rules
- **Title**: Required, minimum 2 characters
- **Author**: Required, minimum 2 characters
- **Status**: Required, must be selected from dropdown

### User Experience
- **Loading States**: Button shows "Saving..." during operation
- **Success Feedback**: Toast message on successful save
- **Error Handling**: Clear error messages for failed operations
- **Navigation**: Automatic return to book list after successful save

## TODO Items
- [ ] Implement book editing functionality
- [ ] Add book search and filtering
- [ ] Implement book borrowing system
- [ ] Add book categories and tags
- [ ] Implement push notifications for book updates
- [ ] Add book cover image upload
- [ ] Implement book rating system 